package s3.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
public class FeedDto {
    public List<PostDto.ResImageListDto> postList;

    @Data
    @Builder
    public static class followingsWhoUploadedStoryDto {
        public String accountId;
        public String profileImage;
    }
}
