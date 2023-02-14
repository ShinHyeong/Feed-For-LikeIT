package s3.feed.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import s3.feed.dto.StoryDto;
import s3.feed.entity.PostEntity;
import s3.feed.entity.StoryEntity;
import s3.feed.entity.UserEntity;
import s3.feed.exception.ForbiddenException;
import s3.feed.repository.StoryRepository;
import s3.feed.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryService {
    private final UserRepository userRepository;
    private final PostService postService;
    private final StoryRepository storyRepository;
    private final AmazonS3 amazonS3;
    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
//    private final ScheduleService scheduleService;

    public StoryEntity uploadStory(MultipartFile multipartFile, String accountId) throws InterruptedException {

        UserEntity userEntity = userRepository.findByAccountId(accountId);

        String fileName = postService.createUuidFileName(multipartFile.getOriginalFilename());
        StoryEntity storyEntity = new StoryEntity(fileName, accountId, userEntity.getProfileImage(), LocalDateTime.now());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
        }

        if(userEntity.getStoryList().size()==0){
            userEntity.getStoryList().add(storyEntity);
            userRepository.save(userEntity);
        }else{ //이미 등록된 스토리가 있을경우 PREVIOUS 관계 추가
            StoryEntity preStory = storyRepository.getLastStory(userEntity.getAccountId());
            userEntity.getStoryList().add(storyEntity);
            userRepository.save(userEntity);
            storyRepository.uploadMoreStory(preStory.getId(), storyEntity.getId());
        }

        System.out.println("시작");
        Thread.sleep(5000);
        System.out.println("끝");
//        scheduleService.schedule();
//   scheduleService.printDate();
//        System.out.println("스케줄러 끝");
        return storyEntity;
    }

    public StoryDto.ResStoryListDto getStory(String accountId, String image) {
        UserEntity userEntity = userRepository.findByAccountId(accountId);
        StoryDto.ResStoryListDto resStoryListDto = new StoryDto.ResStoryListDto();

        List<StoryEntity> storyList = userEntity.getStoryList();
        for (StoryEntity storyEntity : storyList) {
            String storedImageUrl = amazonS3Client.getUrl(bucket, storyEntity.getImage()).toString();
            resStoryListDto.getStoryList().add(new StoryDto.ReqStoryListDto(storedImageUrl, storyEntity.getCreatedDt()));
        }
        return new StoryDto.ResStoryListDto(accountId, storyList.get(0).getProfileImage(), resStoryListDto.getStoryList());
    }

    //    public PostDto.ResImageListDto getImageList(Long postId) {
//        PostDto.ResImageListDto resImageListDto = new PostDto.ResImageListDto();
//        PostEntity postEntity = postRepository.findById(postId).get();
//        List<MediaEntity> mediaEntityList = postEntity.getMediaEntityList();
//        for (MediaEntity mediaEntity : mediaEntityList) {
//            String storedImageUrl = amazonS3Client.getUrl(bucket, mediaEntity.getImage()).toString();
//            resImageListDto.getImageList().add(storedImageUrl);
//        }
//        return new PostDto.ResImageListDto(postEntity.getContent(), postEntity.getUserEntity().getName(), resImageListDto.getImageList());
//    }
    public ResponseEntity deleteStory(Long storyId, String accountId) {

        StoryEntity storyEntity = storyRepository.findById(storyId).get();
        String storyWriter = storyEntity.getAccountId();
        Long storyLevel = storyRepository.getStoryLevel(storyId);
        Long lastStoryId = storyRepository.getLastStory(accountId).getId(); //최신 Story id
        Long storyDepth = storyRepository.getStoryDepth(lastStoryId); //depth (올린 Story 수)
        Long preStoryId= Long.valueOf(0); Long nextStoryId= Long.valueOf(0);

        if(storyDepth>1){ /* 등록된 Story가 2개 이상일 때, preStoryId, nextStoryId */
            if (storyLevel == 1) {
                preStoryId = storyRepository.getPreStory(storyId).getId(); //최신 Story를 삭제하는 경우
            }else if(storyLevel>1 && storyLevel<storyDepth) { // 중간 날짜의 Story를 삭제하는 경우
                preStoryId = storyRepository.getPreStory(storyId).getId();
                nextStoryId = storyRepository.getNextStory(storyId).getId();
            }else { //가장 오래된 Story를 삭제하는 경우
                nextStoryId = storyRepository.getNextStory(storyId).getId();
            }
        }

        if (storyWriter.equals(accountId)) {
            storyRepository.deleteById(storyEntity.getId());
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, storyEntity.getImage()));

            /* 등록된 Story가 2개 이상일 때, 삭제한 후 Story간 관계 처리 */
            //최신 Story를 삭제하는 경우 - 다시 UPLOADED_LAST 관계로 연결시켜야 함
            if(storyDepth>1) {
                if (storyLevel == 1) {
                    storyRepository.setUploadLast(accountId, preStoryId);
                    // 중간 날짜의 Story를 삭제하는 경우 - 다시 PREVIOUS 관계로 연결시켜야함
                } else if (storyLevel > 1 && storyLevel < storyDepth) {
                    storyRepository.setPrevious(preStoryId, nextStoryId);
                } else {//가장 오래된 Story를 삭제하는 경우 - 아무것도 하지 않음
                }

            }
        } else {
            throw new ForbiddenException("권한이 없습니다.");
        }
        return ResponseEntity.ok("스토리 삭제 성공~");
    }
}

