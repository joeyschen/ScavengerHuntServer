package Server;

import Objects.*;
import Util.DBUtil.DBConnector;
import RequestMethods.Gets;
import RequestMethods.Posts;
import Serializer.Serializer;
import com.google.gson.Gson;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import spark.Request;

import java.util.List;

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
            setDBConnectors(DBConnector.getInstance(args[1]));
        } catch (ConfigurationException e) {
            throw new IllegalArgumentException("Config File was not an acutal config file!");
        }
        externalStaticFileLocation(htmlLocation);
        port(port);


        //This is a test
        get("/HelloWorld", (request, response) -> {
            return "Hello World";
        });

        post("/books", (request, response) -> {
            //   System.out.println(request.body());
            String author = request.queryParams("author");
            String title = request.queryParams("title");
            return author + " " + title;

        });


        /*------------ posts ------------*/
        post("/Login", (request, response) -> {
            LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);
            LoginResponse loginResponse = new LoginResponse();
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();
            boolean login_success = Posts.Login(username, password);
            loginResponse.setSuccess(login_success);
            if (login_success) {
                loginResponse.setFriendRequests(Gets.GetNumberOfFriends(username));
            }
            return Serializer.toJson(loginResponse);
        });

        post("/AddUser", ((request, response) -> {
            SignUpRequest signUpRequest = gson.fromJson(request.body(), SignUpRequest.class);
            return String.valueOf(Posts.AddUser(signUpRequest));
        }));

        post("/PlacePhoto", (((request, response) -> {
            return String.valueOf(Posts.PlacePhoto(request));
        })));

        post("/FriendRequestResponse", (((request, response) -> {
            return String.valueOf(Posts.UpdateFriendRequest(gson.fromJson(request.body(), FriendRequestRequest.class)));
        })));

        post("/AddTopic", ((request, response) -> {
            TopicRequest topicRequest = gson.fromJson(request.body(), TopicRequest.class);
            return String.valueOf(Posts.AddTopic(topicRequest));
        }));

        post("/AddRating", ((request, response) -> {
            RatingRequest ratingRequest = gson.fromJson(request.body(), RatingRequest.class);
            return String.valueOf(Posts.AddRating(ratingRequest));
        }));

        /*------------ gets ------------*/
        get("/GetFriendRequests", ((request, response) -> {
           List<String> friendRequests = Gets.GetFriendRequests(request.queryParams("username"));
            return Serializer.toJson(friendRequests);
        }));

        get("/GetFriends", ((request, response) -> {
            List<String> friends = Gets.GetFriends(request.queryParams("username"));
            return Serializer.toJson(friends);
        }));

        get("/GetFriendPage", (((request, response) -> {
            FriendPageResponse friendPageResponse = Gets.GetFriendResponse(request.queryParams("username"), request.queryParams("friend"));
            return Serializer.toJson(friendPageResponse);
        })));


    }

    private static void getConfigurations(String file) throws ConfigurationException {
        PropertiesConfiguration propertiesConfig = new PropertiesConfiguration(file);
        port = propertiesConfig.getInt("port");
        htmlLocation = propertiesConfig.getString("html.location");
        setFileUtils(propertiesConfig.getString("user.location"));
    }

    private static void setDBConnectors(DBConnector connector) {
        Posts.setDBConnector(connector);
        Gets.setDBConnector(connector);
    }

    private static void setFileUtils(String userDirectory) {
        Posts.setFileUtils(userDirectory);
    }


}
