package Objects;

/**
 * Created by derekhsieh on 4/6/16.
 */
public class TopicRequest {
    private String username;
    private String friend;
    private String topic;
    private long updateTime;

    public TopicRequest(String username, String friend, String topic, long updateTime) {
        this.username = username;
        this.friend = friend;
        this.topic = topic;
        this.updateTime = updateTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
