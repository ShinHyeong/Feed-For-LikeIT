package s3.feed.dto;

import lombok.Builder;

import java.util.List;

@Builder
public class FeedDto {
    public List<PostDto.ResImageListDto> postList;
}
