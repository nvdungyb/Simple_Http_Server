package config;

import lombok.Getter;

import java.util.Map;

@Getter
public class TargetResources {
    private Map<String, String> resources;

    public TargetResources(Map<String, String> resources) {
        this.resources = resources;
    }

    public Map<String, String> getResources() {
        return resources;
    }

    public void setResources(Map<String, String> resources) {
        this.resources = resources;
    }

    @Override
    public String toString() {
        return "TargetResources{" +
                "resources=" + resources.keySet().stream()
                .map(key -> key + ": " + resources.get(key) + ", \n")
                .toList()
                + '}';
    }
}
