package yurykorzun.art.universe.music.data.semantic.applicator.applier;

import java.util.HashMap;
import java.util.Map;

public class ApplicationContext {

    private final Map<String, Long> synthIdMap = new HashMap<>();

    public void registerSynthId(String synthId, Long realId) {
        if (synthId != null) {
            synthIdMap.put(synthId, realId);
        }
    }

    public Long resolveSynthId(String synthId) {
        return synthIdMap.get(synthId);
    }

    public Map<String, Long> synthIdMap() {
        return synthIdMap;
    }
}
