package s3.feed.repository;


import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import s3.feed.entity.ReplyEntity;

public interface ReplyRepository extends Neo4jRepository<ReplyEntity, Long> {

    @Query("MATCH (a:Account{ accountId:$accountId })" +
            "MATCH (r:Reply) WHERE id(r)=$id " +
            "CREATE (a)-[:WRITES]->(r) ")
    void write(String accountId, Long id);

    //좋아요 취소
    @Query("MATCH (reply:Reply) WHERE id(reply)=$replyId " +
            "MATCH (:Account{ accountId:$accountId })-[r:LIKES]->(reply) " +
            "DELETE r")
    void deleteLike(String accountId, Long replyId);

    //좋아요 여부 확인
    @Query("MATCH (r:Reply) WHERE id(r)=$replyId " +
            "MATCH (a:Account{ accountId:$accountId }) " +
            "RETURN EXISTS((a)-[:LIKES]->(r))")
    boolean isLike(String accountId, Long replyId);
}
