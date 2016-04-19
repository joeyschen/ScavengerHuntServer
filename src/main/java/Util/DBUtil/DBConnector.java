package Util.DBUtil;

import Objects.FriendPageResponse;
import Serializer.Serializer;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;


import java.beans.PropertyVetoException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by derekhsieh on 6/7/15.
 */

/*
* DBConnector with appropriate methods to get/update/insert into specific tables
 */
public class DBConnector {
    private static Logger logger = Logger.getLogger(DBConnector.class);
    private static DataSource dataSource;
    private static volatile DBConnector dbconnector;
    private static Object syncObject = new Object();
    private MessageDigest hashFunction;

    private DBConnector(String configFile) {
        try {
            logger.info("Creating new datasource");
            dataSource = new DataSource(configFile);
        } catch (PropertyVetoException e) {
            logger.error(e.getMessage(), e);
        } catch (ConfigurationException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static DBConnector getInstance(String file) {
        dbconnector = new DBConnector(file);
        return dbconnector;
    }


    /**
     * LoginRequest query given username and password
     *
     * @param username checks user column of users table
     * @param password checks password column of users table
     * @return True if the username and password combination was found in the database otherwise returns false
     */
    public boolean login(String username, String password) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        logger.info("Starting login for user: " + username);
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select user from users where password = ?");
            statement.setString(1, hash(password));
            set = statement.executeQuery();
            if (set.next()) {
                String user = set.getString(1);
                if (user != null || user.equals(username)) {
                    logger.info("user: " + username + " sucessfully logged in!");
                    return true;
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.info("Closing connection");
            close(connection, statement, set);
        }
        logger.info("user: " + username + " did not sucessfully logged in!");
        return false;
    }

    /**
     * When a user signs up, this is the query to add them to the users table.
     *
     * @param username   user column of the users table
     * @param password   password column of the users table
     * @param email      email column of the users table
     * @param first_name first_name column of the users table
     * @param last_name  last_name column of the users table
     * @return True if successful in adding the user to the users table or false if there was some error
     */
    public boolean addUser(String username, String password, String email, String first_name, String last_name) {
        Connection connection = null;
        PreparedStatement statement = null;
        logger.info("Adding user " + username);
        int added = -1;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("insert into users(user,password,email,first_name,last_name) values(?,?,?,?,?)");
            statement.setString(1, username);
            statement.setString(2, hash(password));
            statement.setString(3, email);
            statement.setString(4, first_name);
            statement.setString(5, last_name);
            added = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }
        if (added < 1) {
            logger.error("Could not add user " + username);
            return false;
        } else {
            logger.info("Successfully added user " + username);
            return true;
        }
    }

    /**
     * Creates a row in friend_request table when someone requests
     * to be friends with another user.
     *
     * @param requestee Person who requests to be friends
     * @param requested Person who was requested to be friends
     * @return Boolean, true if insert was successful and false if not
     */
    public boolean addFriendRequest(String requestee, String requested) {
        Connection connection = null;
        PreparedStatement statement = null;
        int finished = -1;
        logger.info("Adding friend request " + requestee + " to " + requested);
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("insert into friend_request values(?,?)");
            statement.setString(1, requested);
            statement.setString(2, requestee);
            finished = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }

        if (finished != 1) {
            logger.error("Could not add friend request from " + requestee + " to " + requested);
            return false;
        } else {
            logger.info("Successfully added friend request from " + requestee + " to " + requested);
            return true;
        }
    }

    /**
     * When a user decides to check friend request, this is the query to get the
     * friend requests.
     *
     * @param username Username that is used in the where clause of the query
     * @return Returns List of Strings if the table has rows with the user
     */
    public List<String> getFriendRequests(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        List<String> friendRequests = new ArrayList<>();
        logger.info("Starting to get friend requests for user " + username);
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select request from friend_request where user = ?");
            statement.setString(1, username);
            set = statement.executeQuery();
            while (set.next()) {
                friendRequests.add(set.getString(1));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement, set);
        }
        if (friendRequests.size() == 0)
            logger.error("Problem getting friend requests for user " + username);
        else
            logger.info("Got friend requests for user " + username);
        return friendRequests;
    }

    /**
     * When a user accepts or rejects a friend request, query to update the list of friend requests by
     * removing that friend from friend request.
     *
     * @param username username whose friend_requests must be updated
     * @param friend   request is the user who requested to befriend username
     * @return True if deleting that record was a success
     */
    public boolean updateFriendRequest(String username, String friend) {
        Connection connection = null;
        PreparedStatement statement = null;
        int finished = -1;
        logger.info("Updating friend request from user " + username + " and friend " + friend);
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("delete from friend_request where user = ? and request = ? or user = ? and request = ?");
            statement.setString(1, username);
            statement.setString(2, friend);
            statement.setString(3, friend);
            statement.setString(4, username);
            finished = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }
        if (finished < 1) {
            logger.error("Could not update friend request for user " + username + " and friend " + friend);
            return false;
        } else {
            logger.info("Successfuly updated friend request for user " + username + " and friend " + friend);
            return true;
        }
    }

    /**
     * When a user goes to the main page, this query will notify user
     * of how many friend requests that user has to accept or reject.
     *
     * @param username Username used in the where clause of the query to get friend_requests
     * @return Int that tells user how many friend requests that need to be responded to.
     */
    public int getNoFriendRequests(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        int noFriendRequests = 0;
        logger.info("Getting number of friend requests for user " + username);
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select count(*) from friend_request where user = ?");
            statement.setString(1, username);
            set = statement.executeQuery();
            if (set.next()) {
                noFriendRequests = set.getInt(1);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement, set);
        }
        if (noFriendRequests == 0) {
            logger.error("Could not get friend requests for user " + username);
        } else {
            logger.info("Retrieved friend requests for user " + username);
        }
        return noFriendRequests;
    }

    /**
     * When user goes to friend list, this query will retreive all of that users
     * friends.
     *
     * @param username username used in where clause to get the all of that user's friends
     * @return List of users that are friends with username, empty list if something went wrong
     */
    public List<String> getFriends(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        List<String> friendList = new ArrayList<String>();
        logger.info("Getting friends for user " + username);
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select friend from friends where user = ?");
            statement.setString(1, username);
            set = statement.executeQuery();
            while (set.next()) {
                friendList.add(set.getString(1));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement, set);
        }
        return friendList;
    }

    /**
     * When a user adds a a friend from a friend request, this query
     * will add a row into friends table with the user and friend.
     *
     * @param username Username that will be inserted into user column of friends table
     * @param friend   Friend that will be inserted into friend column of friends table
     * @return Returns true if query was successful and false if it was not
     */
    public boolean addFriend(String username, String friend) {
        Connection connection = null;
        PreparedStatement statement = null;
        int finished = 0;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("insert into friends(user,friend) values(?,?)");
            statement.setString(1, username);
            statement.setString(2, friend);
            finished = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }
        if (finished != 1) {
            logger.error("Could not add friend " + friend + " to user " + username);
            return false;
        } else {
            logger.info("Successfully added friend " + friend + " to user " + username);
            return true;
        }
    }

    /**
     * When a user sends a photo, this query will be executed to
     * insert a row into photos table. Row consists of user,friend,
     * photoLocation, and createTime. CreateTime is needed when retrieving
     * the photo at a later date.
     *
     * @param user          User that will be inserted into user column of photos table
     * @param friend        friend tgat will be inserted into friend column of photos table
     * @param photoLocation Location of the photo in the file system
     * @param createTime    Time that the photo was sent to the server
     * @return True if insert was successful and false if not
     */
    public boolean sendPhoto(String user, String friend, String photoLocation, Long createTime) {
        Connection connection = null;
        PreparedStatement statement = null;
        int finished = 0;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("insert into photos values(?,?,?,?)");
            statement.setString(1, user);
            statement.setString(2, friend);
            statement.setString(3, photoLocation);
            statement.setTimestamp(4, new Timestamp(createTime));
            finished = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }
        if (finished != 1)
            return false;
        else
            return true;
    }

    /**
     * When a user checks to see if a friend has sent
     * a photo based on a topic, this is the query that will
     * retrieve the most recent photo sent by the friend. Done
     * by checking the most recent create time.
     *
     * @param user   Used in the where clause to narrow down rows
     * @param friend Used in the where clause to narrow down rows
     * @return String that is the location of the most recent photo, null if query failed.
     */
    public String getPhoto(String user, String friend) {
        Connection connection = null;
        PreparedStatement statement = null;
        String photoLocation = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select photo_location from photos where user = ? " +
                    "and friend = ? order by create_time desc limit 1");
            statement.setString(1, user);
            statement.setString(2, friend);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                photoLocation = set.getString(1);
            } else {
                logger.error("Could not get photo location for user " + user + " from friend " + friend);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }
        return photoLocation;
    }

    /**
     * Gets the topic for a user that a friend has given him.
     *
     * @param username User who has a topic from a user
     * @param friend   Friend who created a topic from the user
     * @return Returns the topic from the row with user and friend, and null if that row does not exist
     */
    public String getTopic(String username, String friend) {
        Connection connection = null;
        PreparedStatement statement = null;
        String topic = null;
        ResultSet set = null;
        logger.info("Getting topic from friend " + friend + " to user " + username);
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select topic from current_hunts where sender = ? and rater = ?");
            statement.setString(1, username);
            statement.setString(2, friend);
            set = statement.executeQuery();
            if (set.next()) {
                topic = set.getString(1);
            } else {
                logger.error("Could not receive topic from " + username + " with friend " + friend + "!");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement, set);
        }
        return topic;
    }

    /**
     * Adds a row into current_hunts when friend sends a topic to a user, done
     * whenever the friend decides to start a hunt with the username.
     *
     * @param username      User who will send the photo
     * @param friend        Friend who created a topic to send to username
     * @param topic         Topic for user to take a photo of
     * @param updateTime    Time friend created topic for username
     * @return              Boolean true if successfully created row, false otherwise
     */
    public boolean insertCurrentHunt(String username, String friend, String topic, long updateTime){
        Connection connection = null;
        PreparedStatement statement = null;
        int finished = -1;
        logger.info("Inserting row into current_hunts by sending topic from " + friend + " to user " + username);
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("insert into current_hunt(sender, rater,topic,updated) values(?,?,?,?)");
            statement.setString(1, username);
            statement.setString(2, friend);
            statement.setString(3, topic);
            statement.setTimestamp(4, new Timestamp(updateTime));
            finished = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }

        if(finished < 1){
            logger.error("Could not create a current hunt from friend " + friend + " to user " + username);
            return false;
        }else{
            logger.info("Successfully added row into current_hunt from friend " + friend + " to user " +username);
            return true;
        }
    }

    /**
     * Update current hunt after username has sent the photo and
     * friend has rated the photo
     *
     * @param username  user who sends the photo
     * @param friend    friend who rates the photo and sends the rating
     * @param rating    rating that friend gives for the photo
     * @return          boolean, true if update was successful annd false otherwise
     */
    public boolean updateCurrentHunt(String username, String friend, double rating){
        Connection connection = null;
        PreparedStatement statement = null;
        int finished = -1;
        logger.info("Updating row into current_hunts containg user " + username + " with friend " + friend);

        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("update current_hunts set rating = ? where sender = ? and rater = ?");
            statement.setDouble(1, rating);
            statement.setString(2, username);
            statement.setString(3, friend);
            finished = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }

        if(finished < 1){
            logger.error("Could not update current_hunts with user " + username + " and " + friend);
            return false;
        }else{
            logger.info("Successfully updated current_hunts with user " + username + " and " + friend);
            return true;
        }
    }

    /**
     *  Get rating for username's photo, which was rated by friend
     *
     * @param username  username who sent the photo and is receiving the photo
     * @param friend    friend who saw the photo and rated it
     * @return          rating the friend gave for the photo
     */
    public double getRating(String username, String friend){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        double rating = -1;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select rating from current_hunts where sender = ? and rater = ?");
            statement.setString(1, username);
            statement.setString(2, friend);
            set = statement.executeQuery();
            if(set.next())
                rating = set.getDouble(1);
            else
                rating = -1;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement, set);
        }

        if(rating == -1)
            logger.error("Could not get rating from friend " + friend + " to user " + username);
        else
            logger.info("Successfully retreived rating from friend " + friend + " to user " + username);

        return rating;
    }

    /**
     *  Once user sees the rating of the hunt, delete the current hunt so friend can start a new one.
     *
     * @param username      sender in the table
     * @param friend        rater in the table
     * @return              true if delete was successful and false otherwise
     */
    public boolean deleteHunt(String username, String friend) {
        Connection connection = null;
        PreparedStatement statement = null;
        int deleted = -1;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("delete from current_hunts where user = ? and friend = ?");
            statement.setString(1, username);
            statement.setString(2, friend);
            deleted = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }

        if (deleted < 1)
            return false;
        else
            return true;
    }

    /**
     * Get the necessary information of the friend based on the user
     *
     * @param username      Username who is getting the information
     * @param friend        Friend who the username wants information on
     * @return              FriendPageResopnse object that contains all the necessary information
     */
    public FriendPageResponse getFriendPageInfo(String username, String friend) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        int huntsPlayed = 0;
        double avgHuntScore = 0.0;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select avg_hunt_score, hunts_played from friends where user = ? and friend = ? ");
            statement.setString(1, username);
            statement.setString(2, friend);
            set = statement.executeQuery();
            logger.info("Getting avg_hunt_score and hunts_played for " + username + " with friend " + friend);
            if (set.next()) {
                avgHuntScore = set.getDouble(1);
                huntsPlayed = set.getInt(2);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement, set);
        }
        return new FriendPageResponse(avgHuntScore, huntsPlayed);
    }

    /**
     * Get the list of friends for a user that do not have a hunt with the user
     *
     * @param username  user that is used in the sql query to get the list of friends
     * @return          list of friends of the user that currently do not have a current hunt going on
     */
    public List<String> friendsToPlayWith(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        List<String> friendsToPlayWith = new ArrayList<>();

        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select friend from friends where friend not in (select rater from current_hunts where sender = ?) and user = ?");
            statement.setString(1, username);
            statement.setString(2, username);
            set = statement.executeQuery();
            while (set.next()) {
                friendsToPlayWith.add(set.getString(1));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement, set);
        }

        return friendsToPlayWith;
    }

    /**
     * Update the row with username and friend to include the score for the user to get at a later time
     *
     * @param username  username who sends the photo
     * @param friend    friend who rates the photo
     * @param score     score that the friend sends
     * @return          Returns true if the update was successful otherwise false
     */
    public boolean updateHuntPlayedWithFriend(String username, String friend, double score) {
        Connection connection = null;
        PreparedStatement statement = null;
        int updated = -1;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("update friends set hunts_played = hunts_played + 1, " +
                    "avg_hunt_score = (avg_hunt_score + ?) / hunts_played where user = ? and friend = ?");
            statement.setDouble(1, score);
            statement.setString(2, username);
            statement.setString(3, friend);
            updated = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement );
        }
        if (updated < 1) {
            return false;
        } else {
            return true;
        }
    }

    private static void close(Connection conn, PreparedStatement statement, ResultSet set) {
        try {
            conn.close();
            statement.close();
            set.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } catch (NullPointerException e) {
            if (conn == null)
                logger.error(e.getMessage(), e);
        }
    }

    private static void close(Connection conn, PreparedStatement statement) {
        try {
            conn.close();
            statement.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    //Hashing Function using MD5 Algorithm
    private String hash(String toHash) throws NoSuchAlgorithmException {
        hashFunction = MessageDigest.getInstance("MD5");
        hashFunction.update(toHash.getBytes());
        byte[] data = hashFunction.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            sb.append(Integer.toString((data[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

}
