package s3.feed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jdk.jshell.Snippet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import s3.feed.dto.UserDto;
import s3.feed.entity.MediaEntity;
import s3.feed.entity.PostEntity;
import s3.feed.entity.StoryEntity;
import s3.feed.entity.UserEntity;
import s3.feed.feign.FeignWithAuth;
import s3.feed.repository.UserRepository;
import s3.feed.service.UserService;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Tag(name = "USER", description = "user controller api")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    FeignWithAuth feignWithAuth;

    @Operation(summary = "마이페이지 조회", description = "마이페이지 조회 api")
    @GetMapping("/mypage/{accountId}")
    public UserDto.ResMypage getMypage(@PathVariable("accountId")String accountId){
        return userService.getMypage(accountId);
    }
    @Operation(summary = "프로필사진 등록", description = "프로필 사진 등록 api")
    @PostMapping("/profileImage/{accountId}")
    public ResponseEntity uploadProfileImage(@PathVariable("accountId") String accountId, @RequestPart MultipartFile multipartFile){
        return new ResponseEntity<>(userService.uploadProfileImage(multipartFile, accountId), HttpStatus.OK);
    }
    @Operation(summary = "프로필 편집", description = "프로필 편집 api")
    @PatchMapping("/mypage/{accountId}")
    public ResponseEntity updateProfile(@PathVariable("accountId")String accountId, @RequestBody UserDto.ReqProfileUpdate reqProfileUpdate){

        return new ResponseEntity<>(userService.updateProfile(accountId,reqProfileUpdate), HttpStatus.OK);
//        feignWithAuth.updateUser(accountId, reqProfileUpdate.getAccountName(),reqProfileUpdate.getAccountId());
    }
    @Operation(summary = "검색 조회", description = "검색 조회 api")
    @GetMapping("/users")
    public List<String>  getUsers(@RequestParam String keyword){
    return  userService.getSearchingUser(keyword);

    }






  /*  @GetMapping("/{accountId}")
    public UserDto.ReqFollowUserDto getFollowList(@PathVariable("accountId") String accountId, @RequestParam String name1,@RequestParam String name2) {
        UserEntity userEntity = userRepository.findByAccountId(accountId);
        List<String> followList = userService.getFollowList(name1, name2);
        UserDto.ReqFollowUserDto reqFollowUserDto = new UserDto.ReqFollowUserDto();
        UserDto.ReqPostDto reqPostDto = new UserDto.ReqPostDto();
        UserDto.ReqFollowStoryDto reqFollowStoryDto = new UserDto.ReqFollowStoryDto();
        reqFollowUserDto.setAccountId(accountId);
        reqFollowUserDto.setProfileImage(userEntity.getProfileImage());
        for(String follow : followList) {
            UserEntity followUser = userRepository.findByAccountId(follow);

            String followUserAccountId = followUser.getAccountId();
            List<StoryEntity> followUserStoryList = followUser.getStoryList();
            String followUserProfileImage = followUser.getProfileImage();
            List<PostEntity> followUserPosts = followUser.getPosts();
            System.out.println("stroy 시작");
            if (followUserStoryList.size() != 0) {
                reqFollowStoryDto = new UserDto.ReqFollowStoryDto(followUserAccountId, followUserProfileImage);
                reqFollowUserDto.getStoryDtoList().add(reqFollowStoryDto);
            }
            if(followUserPosts.size()!=0){
                for (PostEntity postEntity : followUserPosts) {
                    System.out.println("post시작");
                    List<MediaEntity> mediaEntityList = postEntity.getMediaEntityList();
                    String content = postEntity.getContent();
                    LocalDateTime createdDt = postEntity.getCreatedDt();
                    LocalDateTime modifiedDt = postEntity.getModifiedDt();
                    int likeCount = postEntity.getLikeCount();
                    int commentCount = postEntity.getCommentCount();
                    System.out.println("post시작2");
                    reqPostDto = new UserDto.ReqPostDto(followUserAccountId, content, followUserProfileImage, mediaEntityList, createdDt, modifiedDt, likeCount, commentCount);
                    System.out.println("post시작3");
                    System.out.println("postDto: "+reqPostDto);
                    System.out.println("reqFollowUserDto.getPostDtoList(): "+reqFollowUserDto.getPostDtoList());
                    reqFollowUserDto.getPostDtoList().add(reqPostDto);

                }
            }
        }
        userRepository.save(userEntity);
        reqFollowUserDto = new UserDto.ReqFollowUserDto(accountId,userEntity.getProfileImage(),reqFollowUserDto.getStoryDtoList(),reqFollowUserDto.getPostDtoList());
        return reqFollowUserDto;
    }*/













}


//        "name": "user1",
//        "image": "fsdf.png",
//        "followerCount": 100,
//        "followingCount": 95,
//        "postCount": 10,
//        "imageList": [
//        {"image": “sdf.png",
//        “isMultyImage = true”}, {"image": "sd.png,
//        “isMultyImage"=false
//        "}...







//auth 서버에서 회원가입하면 유저 생성
//    @PostMapping("/user")
//    public void createUser(@RequestParam String accountId, @RequestParam String name) {
//
//        UserEntity userEntity = new UserEntity(name, accountId);
//        userService.saveUser(userEntity);
//    }





