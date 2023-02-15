package s3.feed.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import s3.feed.controller.StoryController;
import s3.feed.dto.FeedDto;
import s3.feed.dto.PostDto;
import s3.feed.dto.SlicedResult;
import s3.feed.dto.StoryDto;
import s3.feed.entity.PostEntity;
import s3.feed.entity.StoryEntity;
import s3.feed.entity.UserEntity;
import s3.feed.repository.PostRepository;
import s3.feed.repository.StoryRepository;
import s3.feed.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FeedService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    StoryRepository storyRepository;
    @Autowired
    PostService postService;
    @Autowired
    StoryService storyService;

    //게시물
    public SlicedResult<PostDto.ResImageListDto> searchPostBySlice(String accountId, long lastSeenPostId, int pageSize){
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
        Slice<PostDto.ResImageListDto> slicedPosts = checkLastPageForPost(pageable, results);

        log.info("slicedUnseenPostList={}, pageNum is {}", slicedPosts, slicedPosts.getNumber());

        return SlicedResult.<PostDto.ResImageListDto>builder()
                .pagingState(slicedPosts.getPageable().toString())
                .isLast(slicedPosts.isLast())
                .content(slicedPosts.getContent()).build();
    }

    //스토리
    public SlicedResult<FeedDto.followingsWhoUploadedStoryDto> searchFollowingsWhoUploadedStoryBySlice(String accountId, String lastSeenFollowingId, int pageSize){
        //page 시작번호=0, pageSize(한 페이지에 들어갈 스토리 수)는 클라이언트가 정함
        Pageable pageable = PageRequest.of(0, pageSize);

        //현재 피드에 그려진 마지막 팔로우의 최신 스토리의 생성일
        LocalDateTime storyCreatedDtOfLastSeenFollowing = userRepository.getCreatedDtOfRecentStory(lastSeenFollowingId);


        //그 생성일 이전에 스토리를 올린 팔로우 리스트
        List<UserEntity> unseenFollowingsWhoUploadedStory = userRepository.getUnseenFollowingsWhoUploadedStory(accountId,
                storyCreatedDtOfLastSeenFollowing.getYear(),
                storyCreatedDtOfLastSeenFollowing.getMonthValue(),
                storyCreatedDtOfLastSeenFollowing.getDayOfMonth(),
                storyCreatedDtOfLastSeenFollowing.getHour(),
                storyCreatedDtOfLastSeenFollowing.getMinute(),
                storyCreatedDtOfLastSeenFollowing.getSecond());

        //Dto에 집어넣기
        List<FeedDto.followingsWhoUploadedStoryDto> results = new ArrayList<>();
        for(UserEntity f : unseenFollowingsWhoUploadedStory){
            results.add(FeedDto.followingsWhoUploadedStoryDto.builder()
                    .accountId(f.getAccountId())
                    .profileImage(f.getProfileImage())
                    .build());
        }

        //무한 스크롤 처리를 위한 slices
        Slice<FeedDto.followingsWhoUploadedStoryDto> slicedFollowings = checkLastPageForStory(pageable, results);

        return SlicedResult.<FeedDto.followingsWhoUploadedStoryDto>builder()
                .pagingState(slicedFollowings.getPageable().toString())
                .isLast(slicedFollowings.isLast())
                .content(slicedFollowings.getContent()).build();
    }

    /* 무한 스크롤 방식 처리하는 메소드 */
    //게시물
    private  Slice<PostDto.ResImageListDto> checkLastPageForPost(Pageable pageable, List<PostDto.ResImageListDto> results){
        boolean hasNext = false;
        //보지 않은 게시물 총 갯수 > 요청한 페이지 사이즈 -> 뒤에 더 있는 것으로 처리
        if(results.size()> pageable.getPageSize()){
            hasNext=true;
            results.remove(pageable.getPageSize());
        }
        return new SliceImpl<>(results, pageable, hasNext);
    }

    //스토리
    private  Slice<FeedDto.followingsWhoUploadedStoryDto> checkLastPageForStory(Pageable pageable, List<FeedDto.followingsWhoUploadedStoryDto> results){
        boolean hasNext = false;
        //보지 않은 스토리 총 갯수 > 요청한 페이지 사이즈 -> 뒤에 더 있는 것으로 처리
        if(results.size()> pageable.getPageSize()){
            hasNext=true;
            results.remove(pageable.getPageSize());
        }
        return new SliceImpl<>(results, pageable, hasNext);
    }

    /*프론트 test 용*/
    public FeedDto getPostList(String accountId){
        List<PostEntity> allPostList = postRepository.getPostList(accountId);
        List<PostDto.ResImageListDto> resPostList = new ArrayList<>();
        for(PostEntity p : allPostList){
            resPostList.add(postService.getImageList(p.getId()));
        }
        return FeedDto.builder().postList(resPostList).build();
    }

    public List<FeedDto.followingsWhoUploadedStoryDto> getFollowingsWhoUploadedStory(String accountId){
        List<UserEntity> followingsWhoUploadedStory = userRepository.getFollowingsWhoUploadedStory(accountId);
        List<FeedDto.followingsWhoUploadedStoryDto> response = new ArrayList<>();
        for(UserEntity f : followingsWhoUploadedStory){
            response.add(FeedDto.followingsWhoUploadedStoryDto.builder()
                        .accountId(f.getAccountId())
                        .profileImage(f.getProfileImage())
                        .build());
        }
        return response;
    }
}
