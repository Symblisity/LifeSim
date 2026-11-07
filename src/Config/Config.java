package Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {

private final Properties properties = new Properties();

    public Config(Path file) throws IOException {
        try (BufferedReader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(in);
        }
    }

    public int getInt(String key) {
        return Integer.parseInt(require(key));
    }

    public double getDouble(String key) {
        return Double.parseDouble(require(key));
    }

    public String getString(String key) {
        return require(key);
    }

    private String require(String key) {
        String value = properties.getProperty(key);
        if(value == null) {
            throw new IllegalStateException("Missing config key: " + key);
        }
        return value.trim();
    }

}
