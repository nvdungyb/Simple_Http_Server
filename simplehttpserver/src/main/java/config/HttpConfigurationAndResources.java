package config;

public class HttpConfigurationAndResources {
    private static HttpConfigurationAndResources httpConfigurationAndResources;
    private HttpConfiguration httpConfiguration;
    private TargetResources targetResources;

    private HttpConfigurationAndResources() {
    }

    public static HttpConfigurationAndResources getInstance() {
        if (httpConfigurationAndResources == null)
            httpConfigurationAndResources = new HttpConfigurationAndResources();
        return httpConfigurationAndResources;
    }

    public void setHttpConfiguration(HttpConfiguration httpConfiguration) {
        this.httpConfiguration = httpConfiguration;
    }

    public void setTargetResources(TargetResources targetResources) {
        this.targetResources = targetResources;
    }

    public HttpConfiguration getHttpConfiguration() {
        return httpConfiguration;
    }

    public TargetResources getTargetResources() {
        return targetResources;
    }

    @Override
    public String toString() {
        return "HttpConfigurationAndResources{" +
                "httpConfiguration=" + httpConfiguration +
                ", targetResources=" + targetResources +
                '}';
    }
}
