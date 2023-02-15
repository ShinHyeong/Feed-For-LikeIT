package s3.feed.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Node(labels = {"Story"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryEntity {
    @Id @GeneratedValue
    private Long id;

    private String image;

    private String accountId;

    private String profileImage;

    private LocalDateTime createdDt;

    @Relationship(type = "LIKES", direction = Relationship.Direction.INCOMING)
    private List<UserEntity> usersWhoLikeThis = new ArrayList<>();

    @JsonBackReference
    private UserEntity userEntity;

    public StoryEntity(String image, String accountId, String profileImage, LocalDateTime createdDt) {
        this.image = image;
        this.accountId=accountId;
        this.profileImage=profileImage;
        this.createdDt = createdDt;

    }
}
