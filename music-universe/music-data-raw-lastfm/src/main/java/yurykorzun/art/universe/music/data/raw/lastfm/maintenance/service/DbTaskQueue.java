package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class DbTaskQueue {

    private final LinkedHashMap<String, Runnable> map = new LinkedHashMap<>();

    public synchronized boolean offerUnique(Runnable task, String key) {
        if (map.containsKey(key)) {
            log.warn("queue already contains task {}", key);
            return false;
        }
        map.put(key, task);
        return true;
    }

    public synchronized boolean offer(Runnable task) {
        map.put(UUID.randomUUID().toString(), task);
        return true;
    }

    public synchronized Runnable poll() {
        Iterator<Map.Entry<String, Runnable>> it = map.entrySet().iterator();
        if (!it.hasNext()) return null;
        Runnable task = it.next().getValue();
        it.remove();
        return task;
    }

    public synchronized boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public synchronized boolean isEmpty() {
        return map.isEmpty();
    }

}
