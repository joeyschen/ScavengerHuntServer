package Objects;

/**
 * Created by derekhsieh on 4/6/16.
 */
public class RatingRequest {
    private String username;
    private String friend;
    private Float ranking;
    private long updated;

    public RatingRequest(String username, String friend, Float ranking, long updated) {
        this.username = username;
        this.friend = friend;
        this.ranking = ranking;
        this.updated = updated;
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

    public Float getRanking() {
        return ranking;
    }

    public void setRanking(Float ranking) {
        this.ranking = ranking;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }
}
