package http1;

public enum HttpStatusCode {
    /* CLIENT errors.*/
    CLIENT_ERROR_400_BAD_REQUEST(400, "Bad request"),
    CLIENT_ERROR_401_METHOD_NOT_ALLOWED(401, "Method not allowed."),
    CLIENT_ERROR_414_URL_TOO_LONG(414, "URL too long."),
    /* Server errors.*/
    SERVER_ERROR_500_INTERNAL_SERVER_ERROR(500, "Internal server error."),
    SERVER_ERROR_501_NOT_IMPLEMENTED(501, "Not implemented."),
    SERVER_ERROR_505_HTTP_VERSION_NOT_SUPPORTED(505, "Http version not supported.");

    public final int STATUS_CODE;
    public final String MESSAGE;

    HttpStatusCode(int STATUS_CODE, String MESSAGE) {
        this.STATUS_CODE = STATUS_CODE;
        this.MESSAGE = MESSAGE;
    }
}
