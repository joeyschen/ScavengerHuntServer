package RequestMethods;

import Objects.FriendPageResponse;
import Util.DBUtil.DBConnector;
import Serializer.Serializer;
import Util.FileUtil.FileUtils;
import org.apache.log4j.Logger;

import java.util.List;


/**
 * Created by derekhsieh on 10/5/15.
 * All of the HTTP GET methods are stored here
 */

public class Gets {
    private static Logger logger = Logger.getLogger(Gets.class);
    private static DBConnector connector;
    private static FileUtils fileUtils;

    //Returns a specific user's friend requests
    public static List<String> GetFriendRequests(String username) {
        List<String> friendRequests = connector.getFriendRequests(username);
        if (friendRequests != null) {
            logger.info("Successfully got " + username + "'s friend requests");
        }
        return friendRequests;
    }

    public static List<String> GetFriends(String username) {
        List<String> friendList = connector.getFriends(username);
        if (friendList != null) {
            logger.info("Successfully got " + username + "'s friend list");
        }
        return friendList;
    }

    public static int GetNumberOfFriends(String username) {
        int friendCount = connector.getNoFriendRequests(username);
        if (friendCount != -1) {
            logger.info("Successfully got " + username + "'s number of friends");
        }
        return friendCount;
    }

    public static FriendPageResponse GetFriendResponse(String username, String friend){
        return connector.getFriendPageInfo(username, friend);
    }

    //Returns a photo that was sent to the user by the friend
    public static String  GetPhoto(String user, String friend) {
        String photoLocation = connector.getPhoto(user, friend);
        byte[] photo = fileUtils.getPhoto(photoLocation);
        return Serializer.toJson(photo);
    }

    public static List<String> GetFriendsToPlayWith(String username){
        return connector.friendsToPlayWith(username);
    }

    public static void setDBConnector(DBConnector dBconnector) {
        connector = dBconnector;
    }

    public static void setFileUtils(FileUtils utils) {
        fileUtils = utils;
    }

    public static void setFileUtils(String userDirectory){
        fileUtils = new FileUtils(userDirectory);
    }
}
