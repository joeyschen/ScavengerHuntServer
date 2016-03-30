package Util.DBUtil;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by derekhsieh on 6/7/15.
 */

/*
* Creating the connection pool for mysql database
 */
public class DataSource {
    private volatile ComboPooledDataSource cpds;
    Logger logger = Logger.getLogger(DataSource.class);

    public DataSource(String configFile) throws PropertyVetoException, ConfigurationException {
        PropertiesConfiguration config = new PropertiesConfiguration(configFile);
        this.cpds = new ComboPooledDataSource();
        cpds.setDriverClass("driver"); //loads the jdbc driver
        cpds.setJdbcUrl(config.getString("url"));
        cpds.setUser(config.getString("username"));
        cpds.setPassword(config.getString("password"));
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
    }



    public Connection getConnection() throws SQLException {
        Connection conn = cpds.getConnection();
        if(conn == null){
            logger.error("conn is null");
        }
        return conn;
    }
}
