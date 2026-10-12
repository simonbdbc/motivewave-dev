package sbdev.joy_ai_project.pattern_strategy_algo.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GlobalConfig {
    private static GlobalConfig instance;
    private Properties props;

    private GlobalConfig() {
        props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("sbdev/joy_ai_project/pattern_strategy_algo/config/global.properties")) {
            if (input == null) throw new RuntimeException("global.properties introuvable dans les resources !");
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Impossible de charger le fichier de configuration", ex);
        }
    }

    public static GlobalConfig getInstance() {
        if (instance == null) {
            instance = new GlobalConfig();
        }
        return instance;
    }

    public String get(String key) {
        return props.getProperty(key);
    }
}
