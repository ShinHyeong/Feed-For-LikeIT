package s3.feed.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import s3.feed.entity.StoryEntity;
import s3.feed.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends Neo4jRepository<UserEntity, Long> {
    UserEntity findByAccountId(String accountId);
    UserEntity findByAccountName(String accountName);
    List<UserEntity> findByAccountIdContaining(String accountId);

    /*피드를 위한 인터페이스*/
    @Query("MATCH (a:Account {accountId:$accountId})-[:IS_FOLLOWING]-(f:Account)-[:UPLOADED]->(fs:Story)\n" +
            "with f, max(fs.createdDt) as m\n" +
            "WHERE datetime({ year: toInteger(substring(toString(m), 0, 4)),\n" +
            "                 month: toInteger(substring(toString(m), 5, 2)),\n" +
            "                  day: toInteger(substring(toString(m), 8, 2)), \n" +
            "                  hour: toInteger(substring(toString(m), 11, 2)),\n" +
            "                  minute: toInteger(substring(toString(m), 14, 2)),\n" +
            "                  second: toInteger(substring(toString(m), 17, 2)) }) \n" +
            "                  < datetime({ year: $year, month: $month, day: $day, hour: $hour, minute: $minute, second: $second })\n" +
            "RETURN f\n" +
            "ORDER BY m DESC")
    List<UserEntity> getUnseenFollowingsWhoUploadedStory(String accountId, int year, int month, int day, int hour, int minute, int second);

    @Query("MATCH (f:Account {accountId:$accountId})-[:UPLOADED]->(fs:Story)\n" +
            "RETURN max(fs.createdDt)")
    LocalDateTime getCreatedDtOfRecentStory(String accountId);

    //스토리를 올린 팔로우들의 프로필 리스트 반환(최근에 스토리를 올린 순으로 리스트 반환)
    @Query("MATCH (a:Account {accountId:$accountId})-[:IS_FOLLOWING]-(f:Account)-[:UPLOADED]->(fs:Story)\n" +
            "with f, max(fs.createdDt) as m\n" +
            "RETURN f\n" +
            "ORDER BY m DESC")
    List<UserEntity> getFollowingsWhoUploadedStory(String accountId);

}

