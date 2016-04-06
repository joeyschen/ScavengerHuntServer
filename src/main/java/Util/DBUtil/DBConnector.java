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
        ResultSet set = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("insert into users(user,password,email,first_name,last_name) values(?,?,?,?,?)");
            statement.setString(1, username);
            statement.setString(2, hash(password));
            statement.setString(3, email);
            statement.setString(4, first_name);
            statement.setString(5, last_name);
            int status = statement.executeUpdate();
            if (status == 1)
                return true;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }
        return false;
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

        if (finished != 1)
            return false;
        else
            return true;
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
        return friendRequests;
    }

    /**
     * When a user accepts or rejects a friend request, query to update the list of friend requests by
     * removing that friend from friend request.
     *
     * @param username username whose friend_requests must be updated
     * @param request  request is the user who requested to befriend username
     * @return True if deleting that record was a success
     */
    public boolean updateFriendRequest(String username, String request) {
        Connection connection = null;
        PreparedStatement statement = null;
        int finished = -1;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("delete from friend_request where user = ? and request = ? or user = ? and request = ?");
            statement.setString(1, username);
            statement.setString(2, request);
            statement.setString(3, request);
            statement.setString(4, username);
            finished = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }
        if (finished < 1)
            return false;
        else
            return true;
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
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select count(*) from friend_request where user = ?");
            statement.setString(1, username);
            set = statement.executeQuery();
            if (set.next()) {
                return set.getInt(1);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement, set);
        }
        return 0;
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
            statement = connection.prepareStatement("insert into friends values(?,?)");
            statement.setString(1, username);
            statement.setString(2, friend);
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
     * @param user   User who has a topic from a user
     * @param friend Friend who created a topic from the user
     * @return Returns the topic from the row with user and friend, and null if that row does not exist
     */
    public String getTopic(String user, String friend) {
        Connection connection = null;
        PreparedStatement statement = null;
        String topic = null;
        ResultSet set = null;

        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select topic from topics where user = ? and friend = ?");
            statement.setString(1, user);
            statement.setString(2, friend);
            set = statement.executeQuery();
            if (set.next()) {
                topic = set.getString(1);
            } else {
                logger.error("Could not receive topic from " + user + " with friend " + friend + "!");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement, set);
        }
        return topic;
    }

    /**
     * Put topic for a user given by the friend
     *
     * @param user    User who has a topic given to by a friend
     * @param friend  Friend who gives the topic to the user
     * @param topic   Topic for the hunt
     * @param updated Timestamp of when the topic was given to the user
     * @return Returns true if it was successful in putting into the table by replace query, false if it did not
     */
    public boolean putTopic(String user, String friend, String topic, long updated) {
        Connection connection = null;
        PreparedStatement statement = null;
        int finished = -1;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("replace into topics values(?,?,?,?)");
            statement.setString(1, user);
            statement.setString(2, friend);
            statement.setString(3, topic);
            statement.setTimestamp(4, new Timestamp(updated));
            finished = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }
        if (finished < 1)
            return false;
        else
            return true;
    }

    /**
     * Gets the most recent rank that the friend gives the user
     *
     * @param user   User who was ranked from the hunt by friend
     * @param friend Friend who gives the rank from the photo sent by the user
     * @return Returns value >= 1 if there is a row between user and friend, -1 if there is an error
     */
    public int getRank(String user, String friend) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        int rank = -1;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select rank from ranks where user = ? and friend = ?");
            statement.setString(1, user);
            statement.setString(2, friend);
            set = statement.executeQuery();
            if (set.next())
                rank = set.getInt(1);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement, set);
        }
        return rank;
    }

    /**
     * Updates the rank the user is given by the friend for the photo
     *
     * @param user    User who gave the photo and is receiving the new rank
     * @param friend  Friend who is giving the new rank to the user
     * @param rank    New int value that friend is giving
     * @param updated Timestamp of when the friend gave the rank
     * @return True if query was successful and false is not
     */
    public boolean updateRank(String user, String friend, int rank, long updated) {
        Connection connection = null;
        PreparedStatement statement = null;
        int finished = -1;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("replace into ranks values(?,?,?,?) ");
            statement.setString(1, user);
            statement.setString(2, friend);
            statement.setInt(3, rank);
            statement.setTimestamp(4, new Timestamp(updated));
            finished = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }

        if (finished < 1)
            return false;
        else
            return true;
    }

    public FriendPageResponse getFriendPageInfo(String user, String friend){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        int huntsPlayed = 0;
        double avgHuntScore = 0.0;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select avg_hunt_score, hunts_played from friends where user = ? and friend = ? ");
            set = statement.executeQuery();
            if(set.next()){
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
