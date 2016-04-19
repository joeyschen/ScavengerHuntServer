package Util.FileUtil;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by derekhsieh on 3/27/16.
 */
public class FileUtils {
    //Location of users directory to store photos
    private static String USERS_DIRECTORY ;
    private static Logger logger = Logger.getLogger(FileUtils.class);


    public FileUtils(String userDirectory) {
        USERS_DIRECTORY = userDirectory;
    }

    /**
     * Creates a user directory once a user signs up for the game.
     * For now if there is a directory with that user name, spit out an error
     *
     * @param user      name of user which the directory will be named with
     * @return          Boolean that determines if directory was created or not
     */
    public boolean createUserDirecotry(String user) {
        File userDirectory = new File(USERS_DIRECTORY + "/" + user);
        if (!userDirectory.exists()) {
            logger.info("Creating " + user + " directory!");
            userDirectory.mkdir();
            return true;
        } else {
            logger.error(user + " directory is already there!");
            return false  ;
        }
    }

    public String getPhotoLocation(String user, String friend, long createTime) {
           // return "./src/main/resources/user";
         return USERS_DIRECTORY + "/" + user + "/" + friend + "-" + createTime;
    }

    /**
     * Returns byte[] version of the photo if the location provided is an file.
     *
     *
     * @param photoLocation     location of the photo to check
     * @return                  returns content of the photo if was found, otherwise returns null
     */
    public byte[] getPhoto(String photoLocation) {
        File photo = new File(photoLocation);
        if (photo.exists()) {
            logger.info("Found photo at " + photoLocation);
            try {
                byte[] photoContents = org.apache.commons.io.FileUtils.readFileToByteArray(photo);
                org.apache.commons.io.FileUtils.deleteQuietly(photo);
                return photoContents;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        } else {
            logger.error("Could not find the photo at " + photoLocation);
            return null;
        }
    }

    /**
     * Uses apache fileupload library to write file to file system.
     *
     * @param stream        Stream that has the photo
     * @param file          Location of the file that will contain the photo
     * @return              Boolean if was successful in writing the contents to the file
     */
 public boolean placeFile(InputStream stream, File file) {
     if(!file.exists()){
         FileOutputStream fs = null;
         try {
             fs = new FileOutputStream(file);
             //Actually writing of the file
             Streams.copy(stream, fs, true);
             logger.info("Successfuly written " + file.getName());
             fs.close();
             return true;
         } catch (FileNotFoundException e) {
             logger.error(e.getMessage(), e);
         } catch (IOException e) {
            logger.error(e.getMessage(), e);
         }
     }else{
         logger.error(file.getName() + " is already there!");
     }
     return false;
 }

}
