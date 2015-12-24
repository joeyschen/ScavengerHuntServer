package DBUtil;

import com.google.gson.Gson;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;


import java.beans.PropertyVetoException;
import java.sql.*;
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
    private static volatile DBConnector dbconnector = new DBConnector("./conf/sql.properties");
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

    //determine user name 
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

    public List<String> getFriends(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        List<String> friendList = new ArrayList<String>();
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("select friends from friend_requests where user = ?");
            statement.setString(1, username);
            set = statement.executeQuery();
            while (set.next()) {
                friendList.add(set.getString(1));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return friendList;
    }

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

    //TODO:change table name
    public boolean sendPhoto(String toWho, byte[] photo) {
        Connection connection = null;
        PreparedStatement statement = null;
        int finished = 0;

        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("insert into photos_taken values(?,?)");
            statement.setString(1, toWho);
            Blob picture = connection.createBlob();
            picture.setBytes(1, photo);
            statement.setBlob(2, picture);
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

}
