package Util.DBUtil;

import Serializer.Serializer;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.List;


/**
 * Created by derekhsieh on 4/1/16.
 */
public class DBConnectorTest {
    private DBConnector connector = DBConnector.getInstance(dbProperties);
    private static final String dbProperties = "./src/test/resources/db.properties";
    private static Logger logger = Logger.getLogger(DBConnectorTest.class);

    @Test
    public void login() throws Exception {
        boolean success = connector.login("test", "tester");
        logger.info(success);
    }

    @Test
    public void addUser() throws Exception {
        boolean success = connector.addUser("check", "checker", "check@check.com", "check", "O'checker");
        logger.info(success);
    }

    @Test
    public void addFriendRequest() {
        boolean success = connector.addFriendRequest("quiz", "test");
        logger.info(success);
    }

    @Test
    public void getFriendRequests() throws Exception {
        List<String> requests = connector.getFriendRequests("test");
        logger.info(Serializer.toJson(requests));
    }

    @Test
    public void updateFriendRequests() throws Exception {
        boolean success = connector.updateFriendRequest("test", "quiz");
        logger.info(success);
    }

    @Test
    public void getNoFriendRequests() throws Exception {
        int numRequests = connector.getNoFriendRequests("test");
        logger.info(numRequests);
    }

    @Test
    public void getFriends() throws Exception {
        List<String> friends = connector.getFriends("test");
        logger.info(Serializer.toJson(friends));
    }

    @Test
    public void addFriend() throws Exception {
        boolean success = connector.addFriend("quiz", "test");
        logger.info("success");
    }

    @Test
    public void sendPhoto() throws Exception {
        boolean success = connector.sendPhoto("test", "quiz", "/this/location", System.currentTimeMillis());
        logger.info(success);
    }

    @Test
    public void getPhoto() throws Exception {
        String location = connector.getPhoto("test", "quiz");
        logger.info(location);
    }

    @Test
    public void getTopic() throws Exception {
        String topic = connector.getTopic("test", "quiz");
        logger.info(topic);
    }

    @Test
    public void putTopic() throws Exception {
        boolean success = connector.putTopic("test", "quiz", "cat", System.currentTimeMillis() );
        logger.info(success);
    }

    @Test
    public void getRank() throws Exception {
        int rank = connector.getRating("test", "quiz");
        logger.info(rank);
    }

    @Test
    public void updateRank() throws Exception {
        boolean success = connector.updateRating("test", "quiz", 2, System.currentTimeMillis());
        logger.info(success);

    }

}