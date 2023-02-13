package s3.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import s3.feed.entity.CommentEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class PostDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResImageListDto {
        public Long postId;
        public String content;
        public String accountId;
        public String profileImage;

        private LocalDateTime createdDt;

        private LocalDateTime modifiedDt;

        private int likeCount;
        private int commentCount;
        private boolean isMultyImage;
        private boolean isLikesCheck;
        public List<String> imageList = new ArrayList<>();

         public List<ReqCommentListDto> commentList = new ArrayList<>();
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReqCommentListDto{
        public String commentWriter;
        public String image;
        public String comment;
        public Long commentId;
        public int likeCount;
        public LocalDateTime createdDt;
        public List<ReqReplyDto> replyList = new ArrayList<>();
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReqReplyDto{
        public String replyWriter;
        public String reply;
        public LocalDateTime createdDt;
    }
}
