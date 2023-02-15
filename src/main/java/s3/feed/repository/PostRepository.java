package s3.feed.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import s3.feed.entity.PostEntity;
import s3.feed.entity.StoryEntity;

import java.util.List;

public interface PostRepository extends Neo4jRepository<PostEntity, Long> {
    /*피드를 위한 인터페이스*/
    //무한 스크롤을 위한 인터페이스 : 자신을 포함한 팔로우한 사람들의 게시물 리스트 반환
    @Query("MATCH (p:Post)<-[:UPLOADED]-(a:Account {accountId:$accountId})-[:IS_FOLLOWING]->(f:Account)-[:UPLOADED]->(fp:Post)\n" +
            "unwind [p,fp] as posts\n" +
            "WITH posts\n" +
            "WHERE datetime({ year: toInteger(substring(toString(posts.createdDt), 0, 4)),\n" +
            "                 month: toInteger(substring(toString(posts.createdDt), 5, 2)),\n" +
            "                  day: toInteger(substring(toString(posts.createdDt), 8, 2)), \n" +
            "                  hour: toInteger(substring(toString(posts.createdDt), 11, 2)),\n" +
            "                  minute: toInteger(substring(toString(posts.createdDt), 14, 2)),\n" +
            "                  second: toInteger(substring(toString(posts.createdDt), 17, 2)) }) \n" +
            "                  < datetime({ year: $year, month: $month, day: $day, hour: $hour, minute: $minute, second: $second })\n" +
            "RETURN DISTINCT posts\n" +
            "ORDER BY posts.createdDt DESC ")
    List<PostEntity> getUnseenPostList(String accountId, int year, int month, int day, int hour, int minute, int second);


    //자신을 포함한 팔로우한 사람들의 게시물 리스트 반환
    @Query("MATCH (p:Post)<-[:UPLOADED]-(a:Account {accountId:$accountId})-[:IS_FOLLOWING]->(f:Account)-[:UPLOADED]->(fp:Post)\n" +
            "unwind [p,fp] as posts\n" +
            "RETURN DISTINCT posts\n" +
            "ORDER BY posts.createdDt DESC")
    List<PostEntity> getPostList(String accountId);

    //좋아요 취소
    @Query("MATCH (p:Post) WHERE id(p)=$postId " +
            "MATCH (:Account{ accountId:$accountId })-[r:LIKES]->(p) " +
            "DELETE r")
    void deleteLike(String accountId, Long postId);

    //좋아요 여부 확인
    @Query("MATCH (p:Post) WHERE id(p)=$postId " +
            "MATCH (a:Account{ accountId:$accountId }) " +
            "RETURN EXISTS((a)-[:LIKES]->(p))")
    boolean isLike(String accountId, Long postId);
//    // 자신을 포함한 팔로우한 사람들의 게시물 리스트 중 마지막에 위치한 게시물(가장 오래된) 반환
//    @Query("MATCH (a:Account {accountId:$accountId})-[:UPLOADED_LAST]->(np:Post)\n" +
//            "MATCH (np)-[:PREVIOUS]->(pnp:Post)\n" +
//            "MATCH (a)-[:IS_FOLLOWING]->(f:Account)-[:UPLOADED_LAST]->(p:Post)\n" +
//            "MATCH (p)-[:PREVIOUS]->(pp:Post)\n" +
//            "UNWIND [p, pp, np, pnp] AS posts\n" +
//            "WITH collect(posts) AS posts\n" +
//            "RETURN posts[size(posts)-1]")
//    PostEntity getLastSeenPost(String accountId);
    /*
    //한 사람이 올린 게시물 목록
    @Query( "MATCH (a:Account {accountId:$accountId})-[:UPLOADED_LAST]->(p:Post)\n" +
            "MATCH (p)-[:PREVIOUS]->(pp:Post)\n" +
            "UNWIND [p, pp] AS posts\n" +
            "RETURN posts\n" +
            "ORDER BY posts.createdDt DESC ")
    List<PostEntity> getPostList(String accountId);

    @Query("MATCH (:Account{ accountId:$accountId })-[r:UPLOADED_LAST]->(last:Post)" +
            "RETURN last")
    PostEntity getLastPost(String accountId);

    @Query("MATCH (pre:Post) WHERE id(pre)=$prePostId " +
            "MATCH (:Account)-[r:UPLOADED_LAST]->(pre) " +
            "DELETE r " +
            "WITH pre " +
            "MATCH (last:Post) WHERE id(last)=$lastPostId " +
            "CREATE (last)-[:PREVIOUS]->(pre) ")
    void uploadMorePost(Long prePostId, Long lastPostId);

    @Query("MATCH (last:Post) WHERE id(last)=$id " +
            "MATCH (last)-[r:PREVIOUS]->(pre:Post) " +
            "RETURN pre")
    PostEntity getPrePost(Long id);

    @Query("MATCH (pre:Post) WHERE id(pre)=$id " +
            "MATCH (next:Post)-[r:PREVIOUS]->(pre) " +
            "RETURN next")
    PostEntity getNextPost(Long id);

    @Query("MATCH (a:Account{ accountId:$accountId }) " +
            "MATCH (pre:Post) WHERE id(pre)=$prePostId " +
            "CREATE (pre)<-[:UPLOADED_LAST]-(a) ")
    void setUploadLast(String accountId, Long prePostId);

    @Query("MATCH (pre:Post) WHERE id(pre)=$prePostId " +
            "MATCH (next:Post) WHERE id(next)=$nextPostId " +
            "CREATE (next)-[:PREVIOUS]->(pre) ")
    void setPrevious(Long prePostId, Long nextPostId);

    @Query("MATCH (p:Post) WHERE id(p)=$id " +
            "MATCH (:Post)-[r:PREVIOUS*]->(p) " +
            "RETURN COUNT(r)+1 ")
    Long getPostLevel(Long id);

    @Query("MATCH (last:Post) WHERE id(last)=$lastPostId " +
            "WITH size((last)-[:PREVIOUS*]->()) as depth " +
            "RETURN depth+1 ")
    Long getPostDepth(Long lastPostId);
*/
}

