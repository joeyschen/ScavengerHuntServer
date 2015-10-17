package RequestMethods;

import DBUtil.DBConnector;
import Serializer.Serializer;
import org.apache.log4j.Logger;


/**
 * Created by derekhsieh on 10/5/15.
 */
public class Gets {
    private static Logger logger = Logger.getLogger(Gets.class);
    private static DBConnector connector = DBConnector.getInstance();

    public static String GetFriendRequests(String username) {
        byte[] friendRequests = connector.getFriendRequests(username);
        if (friendRequests != null) {
            logger.info("Successfully got " + username + "'s friend requests");
        }
        return Serializer.toJson(friendRequests);
    }


}
