package analysis_request;

import redis.RedisService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticResponse {
    public static void analysisResponse(OutputStream outputStream) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>\n");
        htmlBuilder.append("<html lang=\"en\">\n");
        htmlBuilder.append("  <head>\n");
        htmlBuilder.append("    <meta charset=\"UTF-8\" />\n");
        htmlBuilder.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n");
        htmlBuilder.append("    <title>Thống Kê Request</title>\n");
        htmlBuilder.append("    <style>\n");
        htmlBuilder.append("      * {\n");
        htmlBuilder.append("        box-sizing: border-box;\n");
        htmlBuilder.append("        margin: 0;\n");
        htmlBuilder.append("        padding: 0;\n");
        htmlBuilder.append("        font-family: Arial, sans-serif;\n");
        htmlBuilder.append("      }\n");
        htmlBuilder.append("\n");
        htmlBuilder.append("      body {\n");
        htmlBuilder.append("        display: flex;\n");
        htmlBuilder.append("        justify-content: center;\n");
        htmlBuilder.append("        align-items: center;\n");
        htmlBuilder.append("        height: 100vh;\n");
        htmlBuilder.append("        background-color: #f5f6fa;\n");
        htmlBuilder.append("      }\n");
        htmlBuilder.append("\n");
        htmlBuilder.append("      .container {\n");
        htmlBuilder.append("        width: 100%;\n");
        htmlBuilder.append("        max-width: 100%;\n");
        htmlBuilder.append("        text-align: center;\n");
        htmlBuilder.append("        background-color: #fff;\n");
        htmlBuilder.append("        border-radius: 10px;\n");
        htmlBuilder.append("        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);\n");
        htmlBuilder.append("        padding: 20px;\n");
        htmlBuilder.append("         overflow-x: auto;\n");
        htmlBuilder.append("      }\n");
        htmlBuilder.append("\n");
        htmlBuilder.append("      h1 {\n");
        htmlBuilder.append("        color: #34495e;\n");
        htmlBuilder.append("        margin: 0;\n");
        htmlBuilder.append("        padding: 20px;\n");
        htmlBuilder.append("        background-color: #f5f6fa;\n");
        htmlBuilder.append("        z-index: 1;\n");
        htmlBuilder.append("        width: 100%;\n");
        htmlBuilder.append("        box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);\n");
        htmlBuilder.append("      }\n");
        htmlBuilder.append("\n");
        htmlBuilder.append("      .stats-table {\n");
        htmlBuilder.append("        width: 100%;\n");
        htmlBuilder.append("        border-collapse: collapse;\n");
        htmlBuilder.append("        border-radius: 10px;\n");
        htmlBuilder.append("        overflow: hidden;\n");
        htmlBuilder.append("      }\n");
        htmlBuilder.append("\n");
        htmlBuilder.append("      .stats-table thead {\n");
        htmlBuilder.append("        background-color: #3498db;\n");
        htmlBuilder.append("        color: #fff;\n");
        htmlBuilder.append("      }\n");
        htmlBuilder.append("\n");
        htmlBuilder.append("      .stats-table th,\n");
        htmlBuilder.append("      .stats-table td {\n");
        htmlBuilder.append("        padding: 15px;\n");
        htmlBuilder.append("        text-align: center;\n");
        htmlBuilder.append("        border: 1px solid #ddd;\n");
        htmlBuilder.append("      }\n");
        htmlBuilder.append("\n");
        htmlBuilder.append("      .stats-table tbody tr:hover {\n");
        htmlBuilder.append("        background-color: #d1e9ff;\n");
        htmlBuilder.append("      }\n");
        htmlBuilder.append("\n");
        htmlBuilder.append("      .target-row {\n");
        htmlBuilder.append("        background-color: #ffffff;\n");
        htmlBuilder.append("      }\n");
        htmlBuilder.append("\n");
        htmlBuilder.append("      .target-row td {\n");
        htmlBuilder.append("        text-align: left;\n");
        htmlBuilder.append("        padding-left: 50px;\n");
        htmlBuilder.append("      }\n");
        htmlBuilder.append("\n");
        htmlBuilder.append("      .stats-table tr:first-child td {\n");
        htmlBuilder.append("        padding-top: 30px;\n");
        htmlBuilder.append("      }\n");
        htmlBuilder.append("    </style>\n");
        htmlBuilder.append("  </head>\n");
        htmlBuilder.append("  <body>\n");
        htmlBuilder.append("    <div class=\"container\">\n");
        htmlBuilder.append("      <h1>Bảng Thống Kê Request</h1>\n");
        htmlBuilder.append("      <table class=\"stats-table\">\n");
        htmlBuilder.append("        <thead>\n");
        htmlBuilder.append("          <tr>\n");
        htmlBuilder.append("            <th>Host</th>\n");
        htmlBuilder.append("            <th>Target</th>\n");
        htmlBuilder.append("            <th>Number of Requests</th>\n");
        htmlBuilder.append("          </tr>\n");
        htmlBuilder.append("        </thead>\n");
        htmlBuilder.append("        <tbody>\n");

        Map<String, List<StatisticTarget>> analysisResponses = RedisService.getAllRequests();
        analysisResponses.keySet().stream().forEach(val -> {
            List<StatisticTarget> listHostTargets = analysisResponses.get(val).stream().sorted().collect(Collectors.toList());
            int size = listHostTargets.size();
            htmlBuilder.append("          <tr>\n");
            htmlBuilder.append("            <td rowspan=\" " + size + "\">" + getHostName(val) + "</td>\n");
            htmlBuilder.append("            <td>" + listHostTargets.get(size - 1).getTarget() + "</td>\n");
            htmlBuilder.append("            <td>" + listHostTargets.get(size - 1).getNumberRequests() + "</td>\n");
            htmlBuilder.append("          </tr>\n");

            for (int i = 0; i < size - 1; i++) {
                htmlBuilder.append("          <tr>\n");
                htmlBuilder.append("            <td>" + listHostTargets.get(i).getTarget() + "</td>\n");
                htmlBuilder.append("            <td>" + listHostTargets.get(i).getNumberRequests() + "</td>\n");
                htmlBuilder.append("          </tr>\n");
            }
        });

        htmlBuilder.append("        </tbody>\n");
        htmlBuilder.append("      </table>\n");
        htmlBuilder.append("    </div>\n");
        htmlBuilder.append("  </body>\n");
        htmlBuilder.append("</html>");

        try {
            String htmlContent = htmlBuilder.toString();
            String contentType = "Content-Type: text/html";
            String responseHeader = "HTTP/1.1 200 OK" + "\r\n" + contentType + "\r\n" + "Content-Length: " + htmlContent.getBytes().length + "\r\n" + "\r\n";

            outputStream.write(responseHeader.getBytes());
            outputStream.write(htmlContent.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Can not write html to client!");
        }
    }

    private static String getHostName(String val) {
        int index = val.indexOf(":");
        return val.substring(index + 1);
    }
}
