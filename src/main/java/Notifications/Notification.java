package Notifications;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by derekhsieh on 10/8/15.
 */
public interface Notification {
    public List<String> getNotifications();

    public void setNotifications(List<String> notifications);


    public void addNotification(String notification);

    public void removeNotification(String notification);

    public int getNumNotifications();
}
