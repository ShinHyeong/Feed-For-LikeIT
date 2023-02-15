package s3.feed.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import s3.feed.entity.StoryEntity;
import s3.feed.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface StoryRepository extends Neo4jRepository<StoryEntity, Long> {
    @Query("MATCH (s:Story) WHERE id(s)=$storyId " +
            "MATCH (:Account{ accountId:$accountId })-[r:LIKES]->(s) " +
            "DELETE r")
    void deleteLike(String accountId, Long storyId);

    //좋아요 여부 확인
    @Query("MATCH (p:Story) WHERE id(p)=$storyId " +
            "MATCH (a:Account{ accountId:$accountId }) " +
            "RETURN EXISTS((a)-[:LIKES]->(p))")
    boolean isLike(String accountId, Long storyId);
//    /*프론트 test 용*/
//    //무한 스크롤을 위한 인터페이스 : 팔로우한 사람들의 스토리 리스트 반환
//    @Query("MATCH (s:Story)<-[:UPLOADED]-(a:Account {accountId:$accountId})-[:IS_FOLLOWING]->(f:Account)-[:UPLOADED]->(fs:Story)\n" +
//            "unwind [s, fs] as stories\n" +
//            "WITH stories\n" +
//            "WHERE datetime({ year: toInteger(substring(toString(stories.createdDt), 0, 4)),\n" +
//            "                 month: toInteger(substring(toString(stories.createdDt), 5, 2)),\n" +
//            "                  day: toInteger(substring(toString(stories.createdDt), 8, 2)), \n" +
//            "                  hour: toInteger(substring(toString(stories.createdDt), 11, 2)),\n" +
//            "                  minute: toInteger(substring(toString(stories.createdDt), 14, 2)),\n" +
//            "                  second: toInteger(substring(toString(stories.createdDt), 17, 2)) }) \n" +
//            "                  < datetime({ year: $year, month: $month, day: $day, hour: $hour, minute: $minute, second: $second })\n" +
//            "RETURN DISTINCT stories\n" +
//            "ORDER BY stories.createdDt DESC ")
//    List<StoryEntity> getUnseenStoryList(String accountId, int year, int month, int day, int hour, int minute, int second);
//
//
//    //팔로우한 사람들의 스토리 리스트 반환
//    @Query("MATCH (s:Story)<-[:UPLOADED]-(a:Account {accountId:$accountId})-[:IS_FOLLOWING]->(f:Account)-[:UPLOADED]->(fs:Story)\n" +
//            "unwind [s,fs] as stories\n" +
//            "RETURN DISTINCT stories\n" +
//            "ORDER BY stories.createdDt DESC")
//    List<StoryEntity> getAllStoryList(String accountId);

       /*
    //한 사람이 올린 스토리 목록
    @Query( "MATCH (a:Account {accountId:\"bbb\"})-[:UPLOADED_LAST]->(s:Story)\n" +
            "MATCH (s)-[:PREVIOUS*]->(ps:Story)\n" +
            "UNWIND [s, ps] AS stories\n" +
            "RETURN stories ")
    List<StoryEntity> getStoryList(String accountId);

    @Query("MATCH (:Account{ accountId:$accountId })-[r:UPLOADED_LAST]->(last:Story)" +
            "RETURN last")
    StoryEntity getLastStory(String accountId);

    @Query("MATCH (pre:Story) WHERE id(pre)=$preStoryId " +
            "MATCH (:Account)-[r:UPLOADED_LAST]->(pre) " +
            "DELETE r " +
            "WITH pre " +
            "MATCH (last:Story) WHERE id(last)=$lastStoryId " +
            "CREATE (last)-[:PREVIOUS]->(pre) ")
    void uploadMoreStory(Long preStoryId, Long lastStoryId);

    @Query("MATCH (last:Story) WHERE id(last)=$id " +
            "MATCH (last)-[r:PREVIOUS]->(pre:Story) " +
            "RETURN pre")
    StoryEntity getPreStory(Long id);

    @Query("MATCH (pre:Story) WHERE id(pre)=$id " +
            "MATCH (next:Story)-[r:PREVIOUS]->(pre) " +
            "RETURN next")
    StoryEntity getNextStory(Long id);

    @Query("MATCH (a:Account{ accountId:$accountId }) " +
            "MATCH (pre:Story) WHERE id(pre)=$preStoryId " +
            "CREATE (pre)<-[:UPLOADED_LAST]-(a) ")
    void setUploadLast(String accountId, Long preStoryId);

    @Query("MATCH (pre:Story) WHERE id(pre)=$preStoryId " +
            "MATCH (next:Story) WHERE id(next)=$nextStoryId " +
            "CREATE (next)-[:PREVIOUS]->(pre) ")
    void setPrevious(Long preStoryId, Long nextStoryId);

    @Query("MATCH (p:Story) WHERE id(p)=$id " +
            "MATCH (:Story)-[r:PREVIOUS*]->(p) " +
            "RETURN COUNT(r)+1 ")
    Long getStoryLevel(Long id);

    @Query("MATCH (last:Story) WHERE id(last)=$lastStoryId " +
            "WITH size((last)-[:PREVIOUS*]->()) as depth " +
            "RETURN depth+1 ")
    Long getStoryDepth(Long lastStoryId);
 */
}
