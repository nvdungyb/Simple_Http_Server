package enums;

import java.util.Arrays;

public enum HttpMethod {
    GET, HEAD, POST, CONNECT;

    public static final int MAX_LENGTH;

    static {
        MAX_LENGTH = Arrays.stream(HttpMethod.values())
                .map(method -> method.name().length())
                .max(Integer::compareTo)
                .orElse(0);
    }
}
