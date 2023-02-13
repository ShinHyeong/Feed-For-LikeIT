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
    public ResponseEntity getFeed(@PathVariable("accountId") String accountId,
                                  @RequestParam long lastSeenPostId,
                                  @RequestParam int pageSize){
        return new ResponseEntity<>(feedService.searchBySlice(accountId, lastSeenPostId, pageSize), HttpStatus.OK);
    }

    /*프론트 test 용 api*/
    //paginated 되지 않는 상태의 게시물 목록을 가져온다
    @GetMapping("/test/{accountId}/postList")
    public ResponseEntity getFeedTest(@PathVariable("accountId") String accountId){
        return new ResponseEntity<>(feedService.mainPageTest(accountId), HttpStatus.OK);
    }
}
