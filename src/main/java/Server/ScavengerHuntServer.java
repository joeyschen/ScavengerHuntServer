package Server;

import Util.DBUtil.DBConnector;
import Notifications.FriendRequests;
import RequestMethods.Gets;
import RequestMethods.Posts;
import Serializer.Serializer;
import com.google.gson.Gson;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * Created by derekhsieh on 10/4/15.
 */

/*
* Main class to run
*
*/
public class ScavengerHuntServer {
    private static Logger logger = Logger.getLogger(ScavengerHuntServer.class);
    private static Gson gson = new Gson();
    private static int port;
    private static String htmlLocation;

    public static void main(String[] args) {

        if (args.length != 2) {
            throw new IllegalArgumentException("Not enough arguements require config files!");
        }
        try {
            getConfigurations(args[0]);
        } catch (ConfigurationException e) {
            throw new IllegalArgumentException("Config File was not an acutal config file!");
        }


        externalStaticFileLocation("./src/main/resources");
        port(80);
        //This is a test
        get("/HelloWorld", (request, response) -> {
            return "Hello World";
        });


/*------------ posts ------------*/
        post("/Login", (request, response) -> {
            Map<String, String> paramMap = getParametersFromBody(request.body());
            String responseBody = "";
            boolean login_success = Posts.Login(paramMap);
            if (login_success) {
                byte[] serializedFriendRequest = DBConnector.getInstance().getFriendRequests(paramMap.get("username"));
                FriendRequests friendRequests = (serializedFriendRequest != null) ? (FriendRequests) Serializer.toObject(serializedFriendRequest) : null;
                int numFriendRequest = DBConnector.getInstance().getNoFriendRequests(paramMap.get("username"));
                responseBody = login_success + "\t" + numFriendRequest;
                responseBody += (serializedFriendRequest != null) ? "\t" + Serializer.toJson(friendRequests) : "";
            }
            return responseBody;
        });

        post("/AddUser", ((request, response) -> {
            return String.valueOf(Posts.AddUser(request.params("username"), request.params("password"),
                    request.params("email"), request.params("first_name"), request.params("last_name")));
        }));


        post("/AddFriend", ((request, response) -> {
            return String.valueOf(Posts.AddFriend(request.params("username"), request.params("friend")));
        }));

        post("/GetFriends", ((request, response) -> {
            return Gets.GetFriends(request.params("username"));
        }));

        post("/GetPhoto", ((request, response) -> {
            return Gets.GetPhoto(request.params("user"), request.params("friend"));
        }));

        //TODO: make sure this works by checking the app will give the server json string
        post("/UpdateFriendRequest", (((request, response) -> {
            return String.valueOf(Posts.updateFriendRequestList(request.params("username"),
                    gson.fromJson(request.params("friendRequest"), ArrayList.class)));
        })));

        post("/placePhoto", (((request, response) -> {
            return String.valueOf(Posts.placePhoto(request));
        })));

/*------------ gets ------------*/
        get("/FriendRequests", ((request, response) -> {
            return Gets.GetFriendRequests(request.params("username"));
        }));

        get("/HelloWorld", (((request, response) -> {
            return "Hello World";
        })));


    }

    //Convert requestBody that is a string into a map of param and value of that parameter
    private static Map<String, String> getParametersFromBody(String requestBody) {
        String[] firstSplit = requestBody.split("\\&");
        Map<String, String> param2ValueMap = new HashMap<>();
        for (int i = 0; i < firstSplit.length; i++) {
            String[] secondSplit = firstSplit[i].split("\\=");
            logger.info("split is " + Arrays.toString(secondSplit));
            param2ValueMap.put(secondSplit[0], secondSplit[1]);
        }
        return param2ValueMap;
    }

    private static void getConfigurations(String file) throws ConfigurationException {
        PropertiesConfiguration propertiesConfig = new PropertiesConfiguration(file);
        port = propertiesConfig.getInt("port");
        htmlLocation = propertiesConfig.getString("user.location");
    }


}
