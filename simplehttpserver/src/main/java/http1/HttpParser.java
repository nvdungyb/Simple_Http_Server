package http1;

import enums.HttpMethod;
import enums.HttpStatusCode;
import exception.BadHttpVersionException;
import exception.HttpParsingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Logger;

public class HttpParser {
    private final static Logger logger = Logger.getLogger(HttpParser.class.getName());

    private static final int SP = 0x20; // 32
    private static final int CR = 0x0D; // 13
    private static final int LF = 0x0A; // 10

    public HttpRequest parseHttpRequest(InputStream inputStream) throws HttpParsingException {
        HttpRequest request = new HttpRequest();
        try {
            parseRequestLine(inputStream, request);
            parseHeaders(inputStream, request);
            parseBody(inputStream, request);
        } catch (IOException e) {
        }

        return request;
    }

    private void parseRequestLine(InputStream reader, HttpRequest request) throws IOException, HttpParsingException {
        StringBuilder processingDataBuffer = new StringBuilder();

        boolean methodParsed = false;
        boolean requestTargetParsed = false;

        int _byte;
        while ((_byte = reader.read()) >= 0) {
            if (_byte == CR) {
                _byte = reader.read();
                if (_byte == LF) {
                    if (!methodParsed || !requestTargetParsed) {
                        throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                    }

                    try {
                        request.setHttpVersion(processingDataBuffer.toString());
                    } catch (BadHttpVersionException ex) {
                        throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                    }
                    return;
                } else {
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                }
            }

            if (_byte == SP) {
                // Todo process previous data
                if (!methodParsed) {
                    String method = processingDataBuffer.toString();
                    logger.info("Request line method: " + method);
                    request.setMethod(method);
                    processingDataBuffer.delete(0, processingDataBuffer.length());
                    methodParsed = true;
                } else if (!requestTargetParsed) {
                    String target = processingDataBuffer.toString();
                    logger.info("Request line target: " + target);
                    request.setRequestTarget(target);
                    processingDataBuffer.delete(0, processingDataBuffer.length());
                    requestTargetParsed = true;
                } else {
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
                }
            } else {
                processingDataBuffer.append((char) _byte);
                if (!methodParsed && processingDataBuffer.length() > HttpMethod.MAX_LENGTH)
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_414_URL_TOO_LONG);
            }
        }
    }

    private void parseHeaders(InputStream inputStream, HttpRequest request) throws IOException {
        HashMap<String, String> header = new HashMap<>();
        int _byte;
        StringBuilder sb = new StringBuilder();
        while (inputStream.available() > 0 && (_byte = inputStream.read()) >= 0) {
            if (_byte == CR) {
                _byte = inputStream.read();
                if (_byte == LF) {
                    if (sb.length() > 0) {
                        String data = sb.toString();
                        sb.delete(0, sb.length());
                        int indexOfColon = data.indexOf(':');
                        try {
                            String headContent = data.substring(0, indexOfColon);
                            String content = data.substring(indexOfColon + 2);          // remove ':' and SP.
                            header.put(headContent, content);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }
            } else {
                sb.append((char) _byte);
            }
        }

        for (String key : header.keySet()) {
            System.out.println(key + ": " + header.get(key));
        }
        request.setHeader(header);
    }

    private void parseBody(InputStream inputStream, HttpRequest request) throws IOException {
        String boundary = null;
        boolean isBoundary = true;
        boolean isData = false;
        int headerBytes = 0;
        int bytesData = 0;
        int contentLength = 0;
        try {
            contentLength = Integer.parseInt(request.getHeader().get("Content-Length"));
        } catch (Exception e) {
            logger.info("Can not read Content-Length in header!");
        }

        StringBuilder sb = new StringBuilder();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int _byte;
        while (inputStream.available() > 0 && (_byte = inputStream.read()) >= 0) {
            if (!isData) {
                sb.append((char) _byte);
                if (_byte == CR) {
                    _byte = inputStream.read();
                    sb.append((char) _byte);
                    if (_byte == LF) {
                        if (sb.length() > 2) {
                            if (isBoundary) {
                                boundary = sb.toString();
                                isBoundary = false;
                            } else {
                                String content = sb.toString();
                                if (content.contains("filename=")) {
                                    int indexOfFilename = content.indexOf("filename=");
                                    String filename = content.substring(indexOfFilename + 10, content.length() - 3);
                                    request.setFilename(filename);
                                }
                            }
                            headerBytes += sb.length();
                            sb.delete(0, sb.length());
                        } else {
                            headerBytes += 2;
                            bytesData = contentLength - (boundary.getBytes().length + 2 + 2 + headerBytes);
                            isData = true;
                        }
                    }
                }
            } else {
                if (bytesData > 0) {
                    outputStream.write(_byte);
                    bytesData--;
                }
            }
        }

        if (outputStream.size() > 0) request.setData(outputStream.toByteArray());
    }
}
