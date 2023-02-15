package s3.feed.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@Node(labels = {"Reply"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReplyEntity {
    @Id
    @GeneratedValue
    private Long id;

    private  String reply;
    private String accountId;
    private LocalDateTime createdDt;
    private int likeCount;
    private String profileImage;

    @Relationship(type = "LIKES", direction = Relationship.Direction.INCOMING)
    private List<UserEntity> usersWhoLikeThis = new ArrayList<>();

    @JsonBackReference
    private UserEntity userEntity;

    public ReplyEntity(String reply, String accountId, LocalDateTime createdDt, int likeCount, String profileImage) {
        this.reply = reply;
        this.accountId=accountId;
        this.createdDt = createdDt;
        this.likeCount = likeCount;
        this.profileImage=profileImage;
    }
}
