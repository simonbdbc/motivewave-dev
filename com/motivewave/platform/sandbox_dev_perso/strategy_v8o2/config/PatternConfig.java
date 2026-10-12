package sbdev.joy_ai_project.strategy_v8o3.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PatternConfig {
    private static PatternConfig instance;
    private Properties props;

    private PatternConfig() {
        props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("sbdev/joy_ai_project/strategy_v8o3/core_pattern/pattern.properties")) {
            if (input == null)
                throw new RuntimeException("pattern.properties introuvable dans les resources !");
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Impossible de charger pattern.properties", ex);
        }
    }

    public static PatternConfig getInstance() {
        if (instance == null) {
            instance = new PatternConfig();
        }
        return instance;
    }

    public String get(String key) {
        return props.getProperty(key);
    }

    // Optionnel : méthode pour récupérer la liste de toutes les clés patternGroup
    public Properties getAllProperties() {
        return props;
    }
}
