package s3.feed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import s3.feed.dto.CommentDto;
import s3.feed.service.CommentService;

@RestController
@Tag(name = "COMMENT", description = "comment controller api")
public class CommentController {
    @Autowired
    CommentService commentService;

    @Operation(summary = "댓글 생성", description = "댓글 생성 api")
    @PostMapping("/{accountId}/{postId}/comment")
    public ResponseEntity createComments(@PathVariable("accountId")String accountId, @PathVariable("postId")Long postId, @RequestBody CommentDto.ReqCommentDto reqCommentDto){
        return new ResponseEntity<>(commentService.createComment(accountId, postId, reqCommentDto.getComment()),HttpStatus.OK);

    }
    @Operation(summary = "댓글 삭제", description = "댓글 삭제 api")
    @DeleteMapping("/{accountId}/comment/{commentId}")
    public ResponseEntity deleteComments(@PathVariable("accountId")String accountId, @PathVariable("commentId")Long commentId){

        return new ResponseEntity<>(commentService.deleteComment(accountId, commentId), HttpStatus.OK);
    }
    @Operation(summary = "대댓글 생성", description = "대댓글 생성 api")
    @PostMapping("/{accountId}/{commentId}/reply")
    public ResponseEntity createReplies(@PathVariable("accountId")String accountId, @PathVariable("commentId")Long commentId, @RequestBody CommentDto.ReqReplyDto reqReplyDto){
        return new ResponseEntity<>(commentService.createReplies(accountId, commentId, reqReplyDto.getReply()),HttpStatus.OK);
    }
    @Operation(summary = "대댓글 삭제", description = "대댓글 삭 api")
    @DeleteMapping("/{accountId}/reply/{replyId}")
    public ResponseEntity deleteReplies(@PathVariable("accountId")String accountId, @PathVariable("replyId")Long replyId){

        return new ResponseEntity<>(commentService.deleteReply(accountId, replyId), HttpStatus.OK);
    }

    @PostMapping("/{accountId}/commentLike/{commentId}")
    public ResponseEntity likeComment(@PathVariable("commentId") Long commentId, @PathVariable("accountId") String accountId) {
        return new ResponseEntity<>(commentService.likeComment(commentId, accountId),HttpStatus.OK);
    }

    @DeleteMapping("/{accountId}/commentLike/{commentId}")
    public ResponseEntity deleteLikeComment(@PathVariable("commentId") Long commentId, @PathVariable("accountId") String accountId) {
        return new ResponseEntity<>(commentService.deleteLikeComment(commentId, accountId),HttpStatus.OK);
    }

    @PostMapping("/{accountId}/replyLike/{replyId}")
    public ResponseEntity likeReply(@PathVariable("replyId") Long replyId, @PathVariable("accountId") String accountId) {
        return new ResponseEntity<>(commentService.likeReply(replyId, accountId),HttpStatus.OK);
    }

    @DeleteMapping("/{accountId}/replyLike/{replyId}")
    public ResponseEntity deleteLikeReply(@PathVariable("replyId") Long replyId, @PathVariable("accountId") String accountId) {
        return new ResponseEntity<>(commentService.deleteLikeReply(replyId, accountId),HttpStatus.OK);
    }
}

