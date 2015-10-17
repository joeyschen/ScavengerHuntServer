package Notifications;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by derekhsieh on 10/8/15.
 * FriendRequest Object that will be serialized
 */
public class FriendRequests implements Notification {
    private List<String> friendRequests;

    public FriendRequests(){
        this.friendRequests = new ArrayList<>();
    }
    @Override
    public List<String> getNotifications() {
        return this.friendRequests;
    }

    @Override
    public void setNotifications(List<String> notifications) {
        this.friendRequests = notifications;
    }

    @Override
    public void addNotification(String notification) {
        friendRequests.add(notification);
    }

    @Override
    public void removeNotification(String notification) {
        friendRequests.remove(notification);
    }

    @Override
    public int getNumNotifications() {
        return this.friendRequests.size();
    }
}
