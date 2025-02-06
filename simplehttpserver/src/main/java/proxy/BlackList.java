package proxy;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import config.ConfigurationManager;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class BlackList {
    private static BlackList instance;
    public Map<String, List<String>> blacklist;

    public BlackList() {
        blacklist = new HashMap<>();
    }

    public BlackList(Map<String, List<String>> blacklist) {
        this.blacklist = blacklist;
    }

    public static BlackList getInstance() {
        if (instance == null) {
            instance = ConfigurationManager.getInstance().loadConfigurationFile("src/main/resources/blacklist.json", BlackList.class);
        }
        return instance;
    }

    @JsonAnySetter
    public void addBlacklistEntry(String key, List<String> value) {
        this.blacklist.put(key, value);
    }
}
