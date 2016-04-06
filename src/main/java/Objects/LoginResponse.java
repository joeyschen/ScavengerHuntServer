package Objects;

import java.util.List;

/**
 * Created by derekhsieh on 4/3/16.
 */
public class LoginResponse {
    private boolean success;
    private int friendRequests;

    public LoginResponse(){}

    public LoginResponse(boolean success, int friendRequests) {
        this.success = success;
        this.friendRequests = friendRequests;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(int friendRequests) {
        this.friendRequests = friendRequests;
    }

}
