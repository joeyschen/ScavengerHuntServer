package RequestMethods;

import Util.DBUtil.DBConnector;
import Serializer.Serializer;
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
import java.util.List;
import java.util.Map;


/**
 * Created by derekhsieh on 10/4/15.
 * All of the HTTP Posts methods stored here
 */

public class Posts {

    private static Logger logger = Logger.getLogger(Posts.class);
    private static DBConnector connector = DBConnector.getInstance();
    private static FileUtils fileUtils = new FileUtils();

    //standard login, use username and password to check ify it is correct
    public static boolean Login(Map<String, String> paramMap) {
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

    //Adds user and friend to the friends table
    public static boolean AddFriend(String username, String friend) {
        boolean addFriend = connector.addFriend(username, friend);
        logger.info("Adding friend " + friend + " to user " + username);
        return addFriend;
    }

    //Updates the friend request stored in frien_request table
    public static boolean updateFriendRequestList(String username, List<String> friendRequest) {
        if (friendRequest == null) {
            logger.error("Friend Requecest received was null");
            return false;
        }
        byte[] converted = Serializer.toByteArray(friendRequest);
        boolean updateFriendRequest = connector.updateFriendRequests(username, converted);
        logger.info("Updating friend requests of user " + username);
        return updateFriendRequest;
    }

    /**
     * Method that sends the photo received from android to file system.
     * Takes request and from it determines the user, friend, create time,
     *  and the acutal photo. Calls FilesUtil to copy contents to file system.
     *
     * @param request   Request that contains the HTTPServletRequest which is used to get all the information
     * @return          Returns true if it was successful in placing the file, if anything goes wrong before that, returns false
     */
    public static boolean placePhoto(Request request) {
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
                String name = item.getName();
                stream = item.openStream();
                if (item.isFormField()) {
                    if (item.getFieldName() != null) {
                        //receiver of the message
                        if (name.equalsIgnoreCase("user")) {
                            user = Streams.asString(stream);
                        //sender of the message
                        } else if (name.equalsIgnoreCase("friend")) {
                            friend = Streams.asString(stream);
                        } else if(name.equalsIgnoreCase("createTime")){
                            createTime = Long.valueOf(Streams.asString(stream));
                        } else{
                            logger.error(name + " was not expected, expected user,friend,createTime " );
                        }
                    }
                } else {
                    photo = new File(fileUtils.getPhotoLocation(user,friend,createTime));
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


}
