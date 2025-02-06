package exception;

import enums.HttpStatusCode;
import lombok.Data;

@Data
public class HttpParsingException extends Exception {
    private HttpStatusCode errorCode;

    public HttpParsingException(HttpStatusCode errorCode) {
        super(errorCode.MESSAGE);
        this.errorCode = errorCode;
    }
}
