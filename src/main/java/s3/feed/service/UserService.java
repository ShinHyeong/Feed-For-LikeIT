package s3.feed.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import s3.feed.dto.UserDto;
import s3.feed.entity.PostEntity;
import s3.feed.entity.UserEntity;
import s3.feed.exception.ForbiddenException;
import s3.feed.feign.FeignWithAuth;
import s3.feed.repository.PostRepository;
import s3.feed.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final AmazonS3 amazonS3;
    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PostService postService;

    public ResponseEntity uploadProfileImage(MultipartFile multipartFile, String accountId){

        UserEntity userEntity = userRepository.findByAccountId(accountId);

        String fileName = postService.createUuidFileName(multipartFile.getOriginalFilename());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
        }
        String storedImageUrl = amazonS3Client.getUrl(bucket, fileName).toString();
        userEntity.uploadProfileImage(storedImageUrl);

        userRepository.save(userEntity);
        return ResponseEntity.ok("프로필사진 등록");
    }

    public ResponseEntity updateProfile(String accountId, UserDto.ReqProfileUpdate reqProfileUpdate){
        UserEntity userEntity = userRepository.findByAccountId(accountId);
        String updateName = reqProfileUpdate.accountName;
        String updateId = reqProfileUpdate.accountId;
        if(updateId!=null){
            UserEntity byAccountId = userRepository.findByAccountId(updateId);
            if(byAccountId!=null) {
    log.error("error log: 이미 존재하는 id입니다." );
                throw new ForbiddenException("이미 존재하는 id입니다.");
            }
        }
        if(updateId!=null&&updateName!=null) {
            userEntity.updateAllProfile(reqProfileUpdate.getAccountName(), reqProfileUpdate.getAccountId());
        }
        else if(updateId!=null&&updateName==null) {
            userEntity.updateIdProfile(updateId);
        }
        else if(updateId==null&&updateName!=null) {
            userEntity.updateNameProfile(updateName);
        }
        userRepository.save(userEntity);
        return ResponseEntity.ok("프로필 업데이트 ok");
    }

    public UserDto.ResMypage getMypage(String accountId){
        UserEntity userEntity = userRepository.findByAccountId(accountId);
        List<PostEntity> posts = userEntity.getPosts();
        int postCount = posts.size();
        List<UserDto.ReqMypagePostDto> mypagePostDtos = new ArrayList<>();

        for(PostEntity postEntity: posts){
            String image = postEntity.getMediaEntityList().get(0).getImage();
            UserDto.ReqMypagePostDto reqMypagePostDto = new UserDto.ReqMypagePostDto(image, postEntity.getId(), postEntity.isMultyImage());
            mypagePostDtos.add(reqMypagePostDto);
        }

        return UserDto.ResMypage.builder()
                .accountId(accountId)
                .accountName(userEntity.getAccountName())
                .profileImage(userEntity.getProfileImage())
                .postCount(postCount)
                .mypagePostDtos(mypagePostDtos)
                .build();
    }

    public List<String> getSearchingUser(String keyword){
        List<UserEntity> byAccountIdContaining = userRepository.findByAccountIdContaining(keyword);
        List<String> accountIdList = new ArrayList<>();
        for(UserEntity userEntity:byAccountIdContaining){
            String accountId = userEntity.getAccountId();
            accountIdList.add(accountId);
        }
        return accountIdList;
    }
}
  /*  public String accountName;
    public String accountId;
    public String profileImage;
    public int followingCount;
    public int followCount;
    public int postCount;
    public List<UserDto.ReqMypagePostDto> mypagePostDtos;


public static class ReqMypagePostDto {
    public String storedUrl;
    public int postId;
    public boolean isMultyImage;
}*/