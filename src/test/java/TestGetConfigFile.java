import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Created by derekhsieh on 4/1/16.
 */
public class TestGetConfigFile {
    public static void main(String[] args){
        String configFile = "./src/test/resources/server.properties";
        try {
            PropertiesConfiguration config = new PropertiesConfiguration(configFile);
            System.out.println(config.getString("port"));
            System.out.println(config.getString("user.location"));
        } catch (ConfigurationException e) {
            System.out.println(e.getMessage());
        }
    }
}
