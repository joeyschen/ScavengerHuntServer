package Objects;

/**
 * Created by derekhsieh on 4/5/16.
 */
public class FriendRequestRequest {
    private String username;
    private String friend;
    private boolean response;

    public FriendRequestRequest(String username, String friend, boolean response) {
        this.username = username;
        this.friend = friend;
        this.response = response;
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

    public boolean isResponse() {
        return response;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }
}
