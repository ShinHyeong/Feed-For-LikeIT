package s3.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StoryDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReqStoryListDto {
     public String storedStoryImage;
     public LocalDateTime createdDt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResStoryListDto {
        public String accountId;
        public String profileImage;
        public List<ReqStoryListDto> storyList = new ArrayList<>();
    }
}
