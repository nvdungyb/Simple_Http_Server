package config;

import java.util.HashMap;
import java.util.stream.Collectors;

public class TargetResources {
    public HashMap<String, String> resources;

    public TargetResources(HashMap<String, String> resources) {
        this.resources = resources;
    }

    public HashMap<String, String> getResources() {
        return resources;
    }

    public void setResources(HashMap<String, String> resources) {
        this.resources = resources;
    }

    @Override
    public String toString() {
        return "TargetResources{" +
                "resources=" + resources.keySet().stream()
                .map(key -> key + ": " + resources.get(key) + ", ")
                .collect(Collectors.toList())
                + '}';
    }
}
