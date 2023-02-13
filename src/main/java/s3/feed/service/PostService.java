package s3.feed.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import s3.feed.dto.PostDto;
import s3.feed.entity.*;
import s3.feed.exception.ForbiddenException;
import s3.feed.repository.MediaRepository;
import s3.feed.repository.PostRepository;
import s3.feed.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    public ResponseEntity uploadFile(List<MultipartFile> multipartFile, String content, String accountId) {
        UserEntity userEntity = userRepository.findByAccountId(accountId);
        PostEntity post = new PostEntity(content);
        boolean isMultyImage = post.isMultyImage();
        multipartFile.forEach(file -> {

            String fileName = createUuidFileName(file.getOriginalFilename());
            MediaEntity mediaEntity = new MediaEntity(fileName);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            try (InputStream inputStream = file.getInputStream()) {
                amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
            }

            post.getMediaEntityList().add(mediaEntity);

        });
        if (post.getMediaEntityList().size() >= 2)
            isMultyImage = true;
        post.addPost(post.getMediaEntityList(), userEntity.getAccountId(), userEntity.getProfileImage(), LocalDateTime.now(), null, 0, 0, isMultyImage);

        if(userEntity.getPosts().size()==0){
            userEntity.getPosts().add(post);
            userRepository.save(userEntity);
        }else{ //이미 등록된 게시물이 있을경우 PREVIOUS 관계 추가
            PostEntity prePost = postRepository.getLastPost(userEntity.getAccountId());
            userEntity.getPosts().add(post);
            userRepository.save(userEntity);
            postRepository.uploadMorePost(prePost.getId(), post.getId());
        }
        return ResponseEntity.ok("게시물 등록");
    }

    public PostDto.ResImageListDto getImageList(Long postId) {
        PostDto.ResImageListDto resImageListDto = new PostDto.ResImageListDto();
        PostEntity postEntity = postRepository.findById(postId).get();
        List<MediaEntity> mediaEntityList = postEntity.getMediaEntityList();
        for (MediaEntity mediaEntity : mediaEntityList) {
            String storedImageUrl = amazonS3Client.getUrl(bucket, mediaEntity.getImage()).toString();
            resImageListDto.getImageList().add(storedImageUrl);
        }
        List<CommentEntity> commentEntityList = postEntity.getCommentEntityList();
        for(CommentEntity commentEntity:commentEntityList){
            List<ReplyEntity> replyEntityList = commentEntity.getReplyEntityList();
            PostDto.ReqCommentListDto reqCommentListDto = new PostDto.ReqCommentListDto();
            for(ReplyEntity replyEntity: replyEntityList){
                reqCommentListDto.replyList.add(new PostDto.ReqReplyDto(replyEntity.getAccountId(), replyEntity.getReply(), replyEntity.getCreatedDt()));
            }
         resImageListDto.commentList.add(new PostDto.ReqCommentListDto(commentEntity.getAccountId(), commentEntity.getProfileImage(), commentEntity.getComment(),
                 commentEntity.getId(), commentEntity.getLikeCount(),commentEntity.getCreatedDt(),reqCommentListDto.replyList));
        }

        return new PostDto.ResImageListDto(postEntity.getId(), postEntity.getContent(), postEntity.getAccountId(), postEntity.getProfileImage(), postEntity.getCreatedDt(), postEntity.getModifiedDt()
                , postEntity.getLikeCount(), postEntity.getCommentEntityList().size(), postEntity.isMultyImage(), postEntity.isLikesCheck(), resImageListDto.getImageList(), resImageListDto.commentList);
    }

    public ResponseEntity deletePost(Long postId, String accountId) {
        PostEntity postEntity = postRepository.findById(postId).get();
        String postWriter = postEntity.getAccountId();
        Long postLevel = postRepository.getPostLevel(postId);
        Long lastPostId = postRepository.getLastPost(accountId).getId(); //최신 Post id
        Long postDepth = postRepository.getPostDepth(lastPostId); //depth (올린 Post 수)
        Long prePostId= Long.valueOf(0); Long nextPostId= Long.valueOf(0);

        if(postDepth>1){ /* 등록된 Post가 2개 이상일 때, prePostId, nextPostId */
            if (postLevel == 1) {
                prePostId = postRepository.getPrePost(postId).getId(); //최신 Post를 삭제하는 경우
            }else if(postLevel>1 && postLevel<postDepth) { // 중간 날짜의 Post를 삭제하는 경우
                prePostId = postRepository.getPrePost(postId).getId();
                nextPostId = postRepository.getNextPost(postId).getId();
            }else { //가장 오래된 Post를 삭제하는 경우
                nextPostId = postRepository.getNextPost(postId).getId();
            }
        }


        if (postWriter.equals(accountId)) {
            List<MediaEntity> mediaEntityList = postEntity.getMediaEntityList();
            postRepository.deleteById(postId);
            for (MediaEntity media : mediaEntityList) {
                mediaRepository.deleteById(media.getId());
                amazonS3.deleteObject(new DeleteObjectRequest(bucket, media.getImage()));
            }

        /* 등록된 Post가 2개 이상일 때, 삭제한 후 Post간 관계 처리 */
        //최신 Post를 삭제하는 경우 - 다시 UPLOADED_LAST 관계로 연결시켜야 함
        if(postDepth>1) {
            if (postLevel == 1) {
                postRepository.setUploadLast(accountId, prePostId);
                // 중간 날짜의 Post를 삭제하는 경우 - 다시 PREVIOUS 관계로 연결시켜야함
            } else if (postLevel > 1 && postLevel < postDepth) {
                postRepository.setPrevious(prePostId, nextPostId);
            } else {//가장 오래된 Post를 삭제하는 경우 - 아무것도 하지 않음
            }
        }
    }
        else {
            throw new ForbiddenException("권한이 없습니다.");
        }
       return ResponseEntity.ok("게시물 삭제 성공");
    }


    public ResponseEntity updatePost(Long postId, String accountId, String content){

        PostEntity postEntity = postRepository.findById(postId).get();
        String postWriter= postEntity.getAccountId();
        if (!postWriter.equals(accountId)) {
            throw new ForbiddenException("권한이 없습니다.");
        }
        postEntity.updatePost(content, LocalDateTime.now());
        postRepository.save(postEntity);
        return ResponseEntity.ok(postId+"게시물 수정 성공");
    }

    public String createUuidFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    public String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
    public void deleteFile(String fileName) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }
}
//    public UserDto.ResPostListDto getUserPosts(String name){
//        UserEntity userEntity = userRepository.findByName(name);
//        UserDto.ResPostListDto resPostListDto = new UserDto.ResPostListDto(userEntity.getName(), userEntity.getPosts());
//        return resPostListDto;
//
//    }