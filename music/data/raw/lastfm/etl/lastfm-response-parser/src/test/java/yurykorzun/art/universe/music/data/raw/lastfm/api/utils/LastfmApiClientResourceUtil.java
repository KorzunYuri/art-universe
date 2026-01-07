package yurykorzun.art.universe.music.data.raw.lastfm.api.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LastfmApiClientResourceUtil {

    private LastfmApiClientResourceUtil() {}

    private static final Map<String, String> apiClientResponses = new HashMap<>();

    static {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:apiclient/responses/*");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null) {
                    continue;
                }
                int dotIndex = filename.lastIndexOf('.');
                String key = (dotIndex != -1) ? filename.substring(0, dotIndex) : filename;
                String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                apiClientResponses.put(key, content);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load api client sample responses", e);
        }
    }

    public static String getApiClientResponse(String key) {
        return apiClientResponses.get(key);
    }

}
