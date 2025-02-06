package http1;

import enums.HttpMethod;
import enums.HttpStatusCode;
import enums.HttpVersion;
import exception.BadHttpVersionException;
import exception.HttpParsingException;

import java.util.Arrays;
import java.util.HashMap;

public class HttpRequest extends HttpMessage {
    private HttpMethod method;
    private String requestTarget;
    private String originalHttpVersion;         // literal from request.
    private HttpVersion bestCompatibleHttpVersion;
    private HashMap<String, String> header;
    private String filename;
    private byte[] data;

    HttpRequest() {
    }

    public HttpMethod getMethod() {
        return method;
    }

    void setMethod(String methodName) throws HttpParsingException {
        HttpMethod method = Arrays.stream(HttpMethod.values())
                .filter(element -> element.name().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new HttpParsingException(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED));

        this.method = method;
    }

    public void setRequestTarget(String target) throws HttpParsingException {
        if (target == null || target.length() == 0)
            throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_500_INTERNAL_SERVER_ERROR);

        this.requestTarget = target;
    }

    public String getRequestTarget() {
        return this.requestTarget;
    }

    void setHttpVersion(String originalHttpVersion) throws BadHttpVersionException, HttpParsingException {
        this.originalHttpVersion = originalHttpVersion;
        this.bestCompatibleHttpVersion = HttpVersion.getBestCompatibleVersion(originalHttpVersion);
        if (this.bestCompatibleHttpVersion == null)
            throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_505_HTTP_VERSION_NOT_SUPPORTED);
    }

    public HttpVersion getBestCompatibleHttpVersion() {
        return this.bestCompatibleHttpVersion;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setHeader(HashMap<String, String> header) {
        this.header = header;
    }

    public HashMap<String, String> getHeader() {
        return this.header;
    }
}
