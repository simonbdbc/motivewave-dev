package com.monitoralgo.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    public static Properties load(String path) throws IOException {
        Properties props = new Properties();
        // Priorité au fichier du disque, sinon charge depuis le classpath
        try (InputStream input = new FileInputStream(path)) {
            props.load(input);
        } catch (IOException e) {
            // Essaie dans le classpath si pas trouvé sur le disque
            try (InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream(path)) {
                if (input != null) {
                    props.load(input);
                } else {
                    throw e;
                }
            }
        }
        return props;
    }
}
