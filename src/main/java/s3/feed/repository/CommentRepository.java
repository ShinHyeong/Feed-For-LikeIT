package s3.feed.repository;


import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import s3.feed.entity.CommentEntity;


public interface CommentRepository extends Neo4jRepository<CommentEntity, Long> {
    CommentEntity findByAccountId(String accountId);

    @Query("MATCH (a:Account{ accountId:$accountId })" +
            "MATCH (c:Comment) WHERE id(c)=$id " +
            "CREATE (a)-[:WRITES]->(c) ")
    void write(String accountId, Long id);

    //좋아요 취소
    @Query("MATCH (c:Comment) WHERE id(c)=$commentId " +
            "MATCH (:Account{ accountId:$accountId })-[r:LIKES]->(c) " +
            "DELETE r")
    void deleteLike(String accountId, Long commentId);

    //좋아요 여부 확인
    @Query("MATCH (p:Comment) WHERE id(p)=$commentId " +
            "MATCH (a:Account{ accountId:$accountId }) " +
            "RETURN EXISTS((a)-[:LIKES]->(p))")
    boolean isLike(String accountId, Long commentId);
}
