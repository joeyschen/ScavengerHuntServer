package RequestMethods;

import Util.DBUtil.DBConnector;
import Serializer.Serializer;
import Util.FileUtil.FileUtils;
import org.apache.log4j.Logger;


/**
 * Created by derekhsieh on 10/5/15.
 * All of the HTTP GET methods are stored here
 */

public class Gets {
    private static Logger logger = Logger.getLogger(Gets.class);
    private static DBConnector connector = DBConnector.getInstance();
    private static FileUtils fileUtils = new FileUtils();

    //Returns a specific user's friend requests
    public static String GetFriendRequests(String username) {
        byte[] friendRequests = connector.getFriendRequests(username);
        if (friendRequests != null) {
            logger.info("Successfully got " + username + "'s friend requests");
        }
        return Serializer.toJson(friendRequests);
    }

    //Returns a photo that was sent to the user by the friend
    public static String GetPhoto(String user, String friend){
        String photoLocation = connector.getPhoto(user, friend);
        byte[] photo = fileUtils.getPhoto(photoLocation);
        return Serializer.toJson(photo);
    }


}
