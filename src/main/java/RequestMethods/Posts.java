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
public class Posts {

    private static Logger logger = Logger.getLogger(Posts.class);
    private static DBConnector connector = DBConnector.getInstance();

   public static boolean Login(Map<String, String> paramMap)
   {
       boolean login_success = connector.login(paramMap.get("username"), paramMap.get("password"));
       logger.info("Login success for user " + paramMap.get("username") + " is " + login_success);
       return login_success;
   }

    public static boolean AddUser(String username, String password, String email, String first_name, String last_name){
        boolean addUserSucess = connector.addUser(username, password, email, first_name, last_name);
        logger.info("Adding user " + username + " is " + addUserSucess);
        return addUserSucess;
    }




}
