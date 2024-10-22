package config;

import lombok.Data;

@Data
public class HttpConfiguration {
    private int port;
    private String webroot;

    @Override
    public String toString() {
        return "HttpConfiguration{" +
                "port=" + port +
                ", webroot='" + webroot + '\'' +
                '}';
    }
}
