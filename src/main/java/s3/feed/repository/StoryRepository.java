package s3.feed.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import s3.feed.entity.StoryEntity;

public interface StoryRepository extends Neo4jRepository<StoryEntity, Long> {
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

}
