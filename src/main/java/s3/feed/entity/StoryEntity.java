package s3.feed.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;

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


    @JsonBackReference
    private UserEntity userEntity;

    public StoryEntity(String image, String accountId, String profileImage, LocalDateTime createdDt) {
        this.image = image;
        this.accountId=accountId;
        this.profileImage=profileImage;
        this.createdDt = createdDt;

    }
}
