package RequestMethods;

import DBUtil.DBConnector;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by derekhsieh on 10/4/15.
 */

/*
* All the post methods for HTTP requests
 */
public class Posts {

    private static Logger logger = Logger.getLogger(Posts.class);
    private static DBConnector connector = DBConnector.getInstance();

    //standard login, use username and password to check if it is correct
   public static boolean Login(Map<String, String> paramMap)
   {
       boolean login_success = connector.login(paramMap.get("username"), paramMap.get("password"));
       logger.info("Login success for user " + paramMap.get("username") + " is " + login_success);
       return login_success;
   }

    //add user to friends table
    public static boolean AddUser(String username, String password, String email, String first_name, String last_name){
        boolean addUserSuccess = connector.addUser(username, password, email, first_name, last_name);
        logger.info("Adding user " + username + " is " + addUserSuccess);
        return addUserSuccess;
    }

    //send photo from mobile to database
    public static boolean SendPhoto(String user, String friend, String photo){
        boolean sendPhotoSuccess = connector.sendPhoto(user, friend, photo);
        logger.info("Receiving photo from " + user + " to " + friend + " is " + sendPhotoSuccess);
        return sendPhotoSuccess;
    }




}
