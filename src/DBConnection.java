import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton utility class to manage MySQL database connection via JDBC.
 * Credentials are loaded from config.properties (gitignored) instead of
 * being hardcoded in source.
 */
public class DBConnection {

    private static Connection connection = null;

    /** Loads DB properties from config.properties in the project root. */
    private static Properties loadConfig() {
        Properties props = new Properties();
        // Look for config.properties next to the running JAR / in the working directory
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("config.properties not found. Make sure it exists in the project root.");
            e.printStackTrace();
        }
        return props;
    }

    /** Returns a shared Connection instance, creating it if necessary. */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Properties cfg = loadConfig();
                String url  = cfg.getProperty("db.url",  "jdbc:mysql://localhost:3306/smart_attendance?useSSL=false&serverTimezone=UTC");
                String user = cfg.getProperty("db.user", "root");
                String pass = cfg.getProperty("db.password", "");
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, user, pass);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Place mysql-connector-j-*.jar in the lib/ folder.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database. Check config.properties and your MySQL server.");
            e.printStackTrace();
        }
        return connection;
    }

    /** Closes the connection when the application exits. */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
