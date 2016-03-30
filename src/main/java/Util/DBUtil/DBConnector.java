package Util.DBUtil;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;


import java.beans.PropertyVetoException;
import java.io.*;
import java.sql.*;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by derekhsieh on 6/7/15.
 */

/*
* DBConnector with appropriate methods to get/update/insert into specific tables
 */
public class DBConnector {
    private static Logger logger = Logger.getLogger(DBConnector.class);
    private static DataSource dataSource;
    private static volatile DBConnector dbconnector = new DBConnector("/home/phoenix/Code/java/ScavengerHuntServer/src/main/resources/sql.properties");
    private static Object syncObject = new Object();

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

    public static DBConnector getInstance() {
        return dbconnector;
    }


    /**
     * Login query given username and password
     * //TODO change instead of sending password to hash of that password
     * Returns boolean if username and password combination is found in the database
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
            statement.setString(1, password);
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
        } finally {
            logger.info("Closing connection");
            close(connection, statement, set);
        }
        logger.info("user: " + username + " did not sucessfully logged in!");
        return false;
    }

    /**
     * When a user signs up, this is the query to add them to the users table.
     * //TODO change password to hash instead of sending the actual password
     * Returns boolean depending on if adding the user was successful or not
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
            statement = connection.prepareStatement("insert into users values(?,?,?,?,?)");
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, email);
            statement.setString(4, first_name);
            statement.setString(5, last_name);
            int status = statement.executeUpdate();
            if (status == 1)
                return true;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                connection.close();
                statement.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * When a user decides to check friend request, this is the query to get the
     * friend requests. Returns list of friend request from friend_request table as a byte array
     * or null if there is not a row with the user in the user column.
     *
     * @param username Username that is used in the where clause of the query
     * @return Returns byte[] if the query has a row with that username, null if there is not
     */
    public byte[] getFriendRequests(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select requests from friend_requests where user = ?");
            statement.setString(1, username);
            set = statement.executeQuery();
            if (set.next())
                return set.getBytes(1);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                connection.close();
                statement.close();
                set.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * When a user accepts or rejects a friend request, query to update the list of friend requests by
     * removing that friend from friend request. Only returns false if there was some sort of error when
     * running the query
     *
     * @param username              username whose friend_requests must be updated
     * @param serializedRequest     friend_requeset in byte form that will be updated in the friend_requests table
     * @return                      True if updating was a success, otherwise false
     */
    public boolean updateFriendRequests(String username, byte[] serializedRequest) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("update from friend_requests set requests = ? where user = ?");
            statement.setBytes(1, serializedRequest);
            statement.setString(2, username);
            int finished = statement.executeUpdate();
            if (finished == 1)
                return true;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                connection.close();
                statement.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * When a user goes to the main page, this query will notify user
     * of how many friend requests that user has to accept or reject.
     * Returns integer of how many friend requests that user has.
     *
     * @param username      Username used in the where clause of the query to get friend_requests
     * @return              Int that tells user how many friend requests that need to be responded to.
     */
    public int getNoFriendRequests(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select count(*) from friend_requests where user = ?");
            statement.setString(1, username);
            set = statement.executeQuery();
            if (set.next()) {
                return set.getInt(1);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return 0;
    }

    /**
     * When user goes to friend list, this query will retreive all of that users
     * friends. Returns list of friends or null if there was an error when running
     * the query. List is created from the set from the query.
     *
     * @param username     username used in where clause to get the all of that user's friends
     * @return             List of users that are friends with username, empty string if somethign went wrong
     *
     * //TODO return an error or null when it fails
     */
    public List<String> getFriends(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select friend from friends where user = ?");
            statement.setString(1, username);
            set = statement.executeQuery();
            if (set.next())
                return set.getBytes(1);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                connection.close();
                statement.close();
                set.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }

        }
        return null;
    }

    /**
     * When a user adds a a friend from a friend request, this query
     * will add a row into friends table with the user and friend.
     * Returns true if insert was successful or false if it was not
     *
     * @param username      Username that will be inserted into user column of friends table
     * @param friend        Friend that will be inserted into friend column of friends table
     * @return              Returns true if query was successful and false if it was not
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
            try {
                connection.close();
                statement.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
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
     * the photo at a later date. Returns true if successful and false if not
     *
     * @param user              User that will be inserted into user column of photos table
     * @param friend            friend tgat will be inserted into friend column of photos table
     * @param photoLocation     Location of the photo in the file system
     * @param createTime        Time that the photo was sent to the server
     * @return                  True if insert was successful and false if not
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
            if (finished == 1)
                return true;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }
        return false;
    }

    //TODO:change table name
    public String getPhoto(String user, String friend) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set;
        String responseString = null;

        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select photo from photos where user = ? and friend = ?");
            statement.setString(1, user);
            statement.setString(2, friend);
            set = statement.executeQuery();
            if (set.next()) {
                responseString = set.getString(1);
                return responseString;
            }
            else {
                return "No image";
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }
        return responseString;
    }

    /**
     * When a user checks to see if a friend has sent
     * a photo based on a topic, this is the query that will
     * retrieve the most recent photo sent by the friend. Done
     * by checking the most recent create time. Returns photo location
     * and null if not.
     *
     * @param user      Used in the where clause to narrow down rows
     * @param friend    Used in the where clause to narrow down rows
     * @return          String that is the location of the most recent photo, null if query failed.
     */
    public String getPhoto(String user, String friend) {
        Connection connection = null;
        PreparedStatement statement = null;
        String photoLocation = null;
        try {
            statement = connection.prepareStatement("select photo_location from photos where user = ? " +
                    "and friend = ? order by create_time desc limit 1");
            statement.setString(1, user);
            statement.setString(2, friend);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                photoLocation += set.getString(1);
            } else {
                logger.error("Could not get photo location for user " + user + " from friend " + friend);
                throw new SQLException("No photo location found!");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(connection, statement);
        }
        return photoLocation;
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

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

}
