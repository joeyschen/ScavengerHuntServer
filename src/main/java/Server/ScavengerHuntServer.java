package Server;

import DBUtil.DBConnector;
import Notifications.FriendRequests;
import RequestMethods.Gets;
import RequestMethods.Posts;
import Serializer.Serializer;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Created by derekhsieh on 10/4/15.
 */

/*
* Main class to run
*
*/
public class ScavengerHuntServer {
    private static Logger logger = Logger.getLogger(ScavengerHuntServer.class);

    public static void main(String[] args) {

        get("/HelloWorld", (request, response) -> {
            return "Hello World";
        });

        post("/Login", (request, response) -> {
            Map<String, String> paramMap = getParametersFromBody(request.body());
            String responseBody = "";
            boolean login_success = Posts.Login(paramMap);
            if (login_success) {
                byte[] serializedFriendRequest = DBConnector.getInstance().getFriendRequests(paramMap.get("username"));
                FriendRequests friendRequests = (serializedFriendRequest != null) ? (FriendRequests) Serializer.toObject(serializedFriendRequest) : null;
                int numFriendRequest = DBConnector.getInstance().getNoFriendRequests(paramMap.get("username"));
                responseBody = login_success + "\t" + numFriendRequest;
                responseBody += (serializedFriendRequest != null) ?  "\t" + Serializer.toJson(friendRequests) : "";
            }
            return responseBody;
        });

        post("/AddUser", ((request, response) -> {
            return String.valueOf(Posts.AddUser(request.params("username"), request.params("password"),
                    request.params("email"), request.params("first_name"), request.params("last_name")));
        }));

        get("/FriendRequests", ((request, response) -> {
            return Gets.GetFriendRequests(request.params("username"));
        }));

        post("/SendPhoto", (((request, response) -> {
            return String.valueOf(Posts.SendPhoto(request.params("toWho"), request.params("photo").getBytes()));
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


}
