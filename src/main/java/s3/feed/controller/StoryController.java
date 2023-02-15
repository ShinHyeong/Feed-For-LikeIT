package s3.feed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import s3.feed.dto.StoryDto;
import s3.feed.service.StoryService;

@RestController
@Tag(name = "STORY", description = "story controller api")
public class StoryController {
    @Autowired
    StoryService storyService;



    @Operation(summary = "스토리 조회", description = "한명의 유저가 생성한 모든 스토리 조회 api")
    @GetMapping("/story/{accountId}")
    public StoryDto.ResStoryListDto getStory(@PathVariable("accountId")String accountId){
        return  storyService.getStory(accountId);
    }
    @Operation(summary = "스토리 생성", description = "스토리 생성 api")
    @PostMapping("/{accountId}/story")
    public ResponseEntity uploadStory(@RequestPart MultipartFile multipartFile,@PathVariable("accountId") String accountId) throws InterruptedException {

        return new ResponseEntity<>(storyService.uploadStory(multipartFile, accountId), HttpStatus.OK);
    }
    @Operation(summary = "스토리 삭제", description = "스토리 삭제 api")
    @DeleteMapping("/{accountId}/story/{storyId}")
    public ResponseEntity deleteStory(@PathVariable("storyId")Long id, @PathVariable String accountId){

        return new ResponseEntity<>(storyService.deleteStory(id, accountId), HttpStatus.OK);
    }
}

//    @DeleteMapping("/file")
//    public void deleteFile( @RequestParam String fileName) {
//        awsS3Service.deleteFile(fileName);
//    }