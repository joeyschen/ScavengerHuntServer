package RequestMethods;

import Objects.FriendRequestRequest;
import Objects.RatingRequest;
import Objects.SignUpRequest;
import Objects.TopicRequest;
import Util.DBUtil.DBConnector;
import Util.FileUtil.FileUtils;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;
import spark.Request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


/**
 * Created by derekhsieh on 10/4/15.
 * All of the HTTP Posts methods stored here
 */

public class Posts {

    private static Logger logger = Logger.getLogger(Posts.class);
    private static DBConnector connector;
    private static FileUtils fileUtils;

    //standard login, use username and password to check ify it is correct
    public static boolean Login(String username, String password) {
        boolean login_success = connector.login(username, password);
        logger.info("LoginRequest success for user " + username + " is " + login_success);
        return login_success;
    }

    //add user to friends table
    public static boolean AddUser(SignUpRequest signUpRequest) {
        boolean addUserSuccess = connector.addUser(signUpRequest.getUsername(), signUpRequest.getPassword(), signUpRequest.getEmail()
                , signUpRequest.getFirstName(), signUpRequest.getLastName());
        logger.info("Adding user " + signUpRequest.getUsername() + " is " + addUserSuccess);
        return addUserSuccess;
    }

    //Updates the friend request stored in friend_request table, and if user has accepted then add friends
    public static boolean UpdateFriendRequest(FriendRequestRequest request) {
        String username = request.getUsername();
        String friend = request.getFriend();
        boolean updateFriendRequest = connector.updateFriendRequest(username, friend);
        if(request.isResponse()){
            updateFriendRequest = connector.addFriend(username, friend);
            updateFriendRequest = connector.addFriend(friend, username);
        }
        logger.info("Updating friend requests of user " + username);
        return updateFriendRequest;
    }

    public static boolean AddTopic(TopicRequest request){
        boolean addTopic = connector.putTopic(request.getUsername(), request.getFriend(), request.getTopic(), request.getUpdateTime());
        logger.info("Sent topic to " + request.getUsername() + " from " + request.getFriend());
        return addTopic;
    }

    public static boolean AddRating(RatingRequest request){
        boolean addRating = connector.updateRating(request.getUsername(), request.getFriend(), request.getRanking(), request.getUpdated());
        logger.info("Sent rating of photo by " +  request.getUsername() + " from " + request.getFriend());
        return addRating;
    }

    /**
     * Method that sends the photo received from android to file system.
     * Takes request and from it determines the user, friend, create time,
     * and the acutal photo. Calls FilesUtil to copy contents to file system.
     *
     * @param request Request that contains the HTTPServletRequest which is used to get all the information
     * @return Returns true if it was successful in placing the file, if anything goes wrong before that, returns false
     */
    public static boolean PlacePhoto(Request request) {
        long createTime = 0;
        InputStream stream = null;
        ServletFileUpload upload = new ServletFileUpload();
        String user = null;
        String friend = null;
        File photo = null;
        try {
            FileItemIterator fileItemIterator = upload.getItemIterator(request.raw());
            FileItemStream item = null;
            while (fileItemIterator.hasNext()) {
                item = fileItemIterator.next();
                String name = item.getFieldName();
                stream = item.openStream();
                if (item.isFormField()) {
                    if (item.getFieldName() != null) {
                        //receiver of the message
                        if (name.equalsIgnoreCase("username")) {
                            user = Streams.asString(stream);
                            //sender of the message
                        } else if (name.equalsIgnoreCase("friend")) {
                            friend = Streams.asString(stream);
                        } else if (name.equalsIgnoreCase("createTime")) {
                            createTime = Long.valueOf(Streams.asString(stream));
                        } else {
                            logger.error(name + " was not expected, expected user,friend,createTime ");
                        }
                    }
                } else {
                    photo = new File(fileUtils.getPhotoLocation(user, friend, createTime));
                    return fileUtils.placeFile(stream, photo);
                }
            }
        } catch (FileUploadException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static void setDBConnector(DBConnector dbConnector) {
        connector = dbConnector;
    }

    public static void setFileUtils(String userDirectory) {
        fileUtils = new FileUtils(userDirectory);
    }


}
