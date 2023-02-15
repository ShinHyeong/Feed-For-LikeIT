package s3.feed.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import s3.feed.service.FeedService;

import java.util.Optional;

@RestController
public class FeedController {
    @Autowired
    FeedService feedService;

    @GetMapping("/{accountId}/postList")
    public ResponseEntity getPaginatedPostList(@PathVariable("accountId") String accountId,
                                  @RequestParam long lastSeenPostId,
                                  @RequestParam int pageSize){
        return new ResponseEntity<>(feedService.searchPostBySlice(accountId, lastSeenPostId, pageSize), HttpStatus.OK);
    }

    @GetMapping("/{accountId}/storyList")
    public ResponseEntity getPaginatedStoryList(@PathVariable("accountId") String accountId,
                                  @RequestParam String lastSeenFollowingId,
                                  @RequestParam int pageSize){
        return new ResponseEntity<>(feedService.searchFollowingsWhoUploadedStoryBySlice(accountId, lastSeenFollowingId, pageSize), HttpStatus.OK);
    }

    /*프론트 test 용 api*/
    //paginated 되지 않는 상태의 게시물 목록을 가져온다
    @GetMapping("/test/{accountId}/postList")
    public ResponseEntity getPostList(@PathVariable("accountId") String accountId){
        return new ResponseEntity<>(feedService.getPostList(accountId), HttpStatus.OK);
    }
    //paginated 되지 않는 상태 : 스토리를 올린 팔로우들의 프로필 리스트 반환(스토리 조회용)
    @GetMapping("/test/{accountId}/storyList")
    public ResponseEntity getFollowingsWhoUploadedStory(@PathVariable("accountId") String accountId){
        return new ResponseEntity<>(feedService.getFollowingsWhoUploadedStory(accountId), HttpStatus.OK);
    }
}
