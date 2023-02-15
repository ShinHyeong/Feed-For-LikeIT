package s3.feed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import s3.feed.repository.PostRepository;
import s3.feed.repository.UserRepository;
import s3.feed.service.PostService;
import s3.feed.dto.PostDto;


import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "POST", description = "post controller api")
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @GetMapping("")
    public String test() {return "feed server ok";  }

    @Operation(summary = "게시물 조회", description = "특정 게시물 조회 api")
    @GetMapping("/media/{postId}")
    public PostDto.ResImageListDto getPosts(@PathVariable("postId")Long id){
        return postService.getImageList(id);
    }

    @Operation(summary = "게시물 생성", description = "게시물 생성 api")
    @PostMapping("/{accountId}/media")
    public ResponseEntity uploadFile(@RequestPart List<MultipartFile> multipartFile, @RequestParam  String content,@PathVariable("accountId")  String accountId) {

        return new ResponseEntity<>(postService.uploadFile(multipartFile, content, accountId),HttpStatus.OK);
    }

    @Operation(summary = "게시물 수정", description = "게시물 수정 api")
    @PatchMapping("/{accountId}/media/{postId}")
    public ResponseEntity updatePost(@PathVariable("postId")Long postId, @PathVariable("accountId") String accountId, @RequestParam String content){
        return new ResponseEntity<>(postService.updatePost(postId, accountId, content), HttpStatus.OK);
    }

    @Operation(summary = "게시물 삭제", description = "게시물 삭제 api")
    @DeleteMapping("/{accountId}/media/{postId}")
    public ResponseEntity ResponseEntity(@PathVariable("postId")Long id, @PathVariable("accountId") String accountId) {
        return new ResponseEntity<>(postService.deletePost(id, accountId), HttpStatus.OK);
    }

    @PostMapping("/{accountId}/postLike/{postId}")
    public ResponseEntity likePost(@PathVariable("postId") Long postId, @PathVariable("accountId") String accountId) {
        return new ResponseEntity<>(postService.likePost(postId, accountId),HttpStatus.OK);
    }

    @DeleteMapping("/{accountId}/postLike/{postId}")
    public ResponseEntity deleteLikePost(@PathVariable("postId") Long postId, @PathVariable("accountId") String accountId) {
        return new ResponseEntity<>(postService.deleteLikePost(postId, accountId),HttpStatus.OK);
    }
}
//한명의 유저가 올린 모든 게시물 조회
//    @GetMapping("/media")
//    public UserDto.ResPostListDto getUserPosts(@RequestParam String name){
//        return postService.getUserPosts(name);
//    }