package sbdev.joy_ai_project.strategy_v8o1.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static Config instance;
    private Properties props;

    private Config() {
        props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("sbdev/joy_ai_project/strategy_v8o3/config/config.properties")) {
            if (input == null) throw new RuntimeException("config.properties introuvable dans les resources !");
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Impossible de charger le fichier de configuration", ex);
        }
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public String get(String key) {
        return props.getProperty(key);
    }
}
