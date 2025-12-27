package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailConfig {

    private static final String CONFIG_FILE = "/email.properties";

    private static String username;
    private static String password;

    static {
        load();
    }

    private static void load() {
        try (InputStream in = EmailConfig.class.getResourceAsStream(CONFIG_FILE)) {
            Properties props = new Properties();
            if (in != null) {
                props.load(in);
                String encUsername = props.getProperty("email.username");
                if (encUsername != null && !encUsername.isEmpty()) {
                	username = EncryptionUtil.decrypt(encUsername);
                }
                String encPassword = props.getProperty("email.password");
                if (encPassword != null && !encPassword.isEmpty()) {
                    password = EncryptionUtil.decrypt(encPassword);
                }
            } else {
                System.err.println("email.properties not found on classpath");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }
}
