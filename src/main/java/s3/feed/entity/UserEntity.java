package s3.feed.entity;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;


@Getter
@Node(labels = {"Account"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String profileImage;

    private String accountId;

    private String accountName;

    @Relationship(type = "UPLOADED")
    private List<PostEntity> posts = new ArrayList<>();

    @Relationship(type = "UPLOADED")
    private List<StoryEntity> storyList = new ArrayList<>();

    @Relationship(type = "WRITES", direction = Relationship.Direction.OUTGOING)
    private List<CommentEntity> commentList = new ArrayList<>();

    @Relationship(type = "WRITES", direction = Relationship.Direction.OUTGOING)
    private List<ReplyEntity> replyEntityList = new ArrayList<>();

    public UserEntity(String accountName,String accountId){
        this.accountName =accountName;
        this.accountId =accountId;
    }

    public UserEntity(String name) {
        this.accountName = name;
    }

    public void uploadProfileImage(String profileImage){
        this.profileImage=profileImage;
    }
    public void updateAllProfile(String accountName, String accountId){
        this.accountName=accountName;
        this.accountId=accountId;
    }
    public void updateNameProfile(String accountName){
        this.accountName=accountName;
    }
    public void updateIdProfile(String accountId){
        this.accountId=accountId;
    }
}