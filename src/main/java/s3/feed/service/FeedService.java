package s3.feed.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import s3.feed.dto.FeedDto;
import s3.feed.dto.PostDto;
import s3.feed.dto.SlicedResult;
import s3.feed.entity.PostEntity;
import s3.feed.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FeedService {

    @Autowired
    PostRepository postRepository;
    @Autowired
    PostService postService;

    public SlicedResult<PostDto.ResImageListDto> searchBySlice(String accountId, long lastSeenPostId, int pageSize){
        //page 시작번호=0, pageSize(한 페이지에 들어갈 게시물 수)는 클라이언트가 정함
        Pageable pageable = PageRequest.of(0, pageSize);

        //현재 피드에 그려진 마지막 게시물의 생성일
        LocalDateTime lastSeenPostCreatedDt = postRepository.findById(lastSeenPostId).map(PostEntity::getCreatedDt).orElse(null);

        //그 생성일 이전에 생성된 게시물들 리스트
        List<PostEntity> unseenPostList = postRepository.getUnseenPostList(accountId,
                lastSeenPostCreatedDt.getYear(),
                lastSeenPostCreatedDt.getMonthValue(),
                lastSeenPostCreatedDt.getDayOfMonth(),
                lastSeenPostCreatedDt.getHour(),
                lastSeenPostCreatedDt.getMinute(),
                lastSeenPostCreatedDt.getSecond());

        //Dto에 집어넣기
        List<PostDto.ResImageListDto> results = new ArrayList<>();
        for(PostEntity p : unseenPostList){
            results.add(postService.getImageList(p.getId()));
        }

        //무한 스크롤 처리를 위한 slices
        Slice<PostDto.ResImageListDto> slicedPosts = checkLastPage(pageable, results);

        log.info("slicedUnseenPostList={}, pageNum is {}", slicedPosts, slicedPosts.getNumber());

        return SlicedResult.<PostDto.ResImageListDto>builder()
                .pagingState(slicedPosts.getPageable().toString())
                .isLast(slicedPosts.isLast())
                .content(slicedPosts.getContent()).build();
    }

    /* 무한 스크롤 방식 처리하는 메소드 */
    private  Slice<PostDto.ResImageListDto> checkLastPage(Pageable pageable, List<PostDto.ResImageListDto> results){
        boolean hasNext = false;
        //보지 않은 게시물 총 갯수 > 요청한 페이지 사이즈 -> 뒤에 더 있는 것으로 처리
        log.info("results.size()={} and pageSize={}", results.size(), pageable.getPageSize() );
        if(results.size()> pageable.getPageSize()){
            hasNext=true;
            results.remove(pageable.getPageSize());
        }
        return new SliceImpl<>(results, pageable, hasNext);
    }

    /*프론트 test 용*/
    public FeedDto mainPageTest(String accountId){
        List<PostEntity> unseenPostList = postRepository.getAllPostList(accountId);
        List<PostDto.ResImageListDto> response = new ArrayList<>();
        for(PostEntity unseenPost : unseenPostList){
            response.add(postService.getImageList(unseenPost.getId()));
        }
        return FeedDto.builder().postList(response).build();
    }
}
