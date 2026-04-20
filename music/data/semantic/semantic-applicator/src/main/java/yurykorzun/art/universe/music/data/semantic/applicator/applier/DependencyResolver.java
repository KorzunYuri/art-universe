package yurykorzun.art.universe.music.data.semantic.applicator.applier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Component
public class DependencyResolver {

    private static final Logger log = LoggerFactory.getLogger(DependencyResolver.class);

    private final ObjectMapper objectMapper;

    public DependencyResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ProposalRow> topologicalSort(List<ProposalRow> proposals) {
        Map<String, ProposalRow> bySynthId = new HashMap<>();
        for (ProposalRow p : proposals) {
            if (p.getSynthId() != null) {
                bySynthId.put(p.getSynthId(), p);
            }
        }

        Map<Long, ProposalRow> byId = new HashMap<>();
        Map<Long, Set<Long>> dependencies = new HashMap<>();
        for (ProposalRow p : proposals) {
            byId.put(p.getId(), p);
            Set<Long> deps = new HashSet<>();
            for (String ref : extractRefs(p.getPayload())) {
                ProposalRow dep = bySynthId.get(ref);
                if (dep != null && !dep.getId().equals(p.getId())) {
                    deps.add(dep.getId());
                }
            }
            dependencies.put(p.getId(), deps);
        }

        Map<Long, Integer> inDegree = new HashMap<>();
        for (ProposalRow p : proposals) {
            inDegree.put(p.getId(), dependencies.get(p.getId()).size());
        }

        Queue<Long> queue = new LinkedList<>();
        for (var entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<ProposalRow> sorted = new ArrayList<>(proposals.size());
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            sorted.add(byId.get(current));

            for (var entry : dependencies.entrySet()) {
                if (entry.getValue().contains(current)) {
                    int newDegree = inDegree.get(entry.getKey()) - 1;
                    inDegree.put(entry.getKey(), newDegree);
                    if (newDegree == 0) {
                        queue.add(entry.getKey());
                    }
                }
            }
        }

        if (sorted.size() < proposals.size()) {
            List<Long> unresolved = new ArrayList<>();
            Set<Long> sortedIds = new HashSet<>();
            for (ProposalRow p : sorted) sortedIds.add(p.getId());
            for (ProposalRow p : proposals) {
                if (!sortedIds.contains(p.getId())) unresolved.add(p.getId());
            }
            throw new IllegalStateException(
                "Cycle detected in proposal dependencies; unresolved proposal ids=" + unresolved
            );
        }

        return sorted;
    }

    private Set<String> extractRefs(String payloadJson) {
        Set<String> refs = new HashSet<>();
        try {
            JsonNode payload = objectMapper.readTree(payloadJson);
            extractRefsRecursive(payload, refs);
        } catch (Exception e) {
            log.warn("Failed to extract refs from payload", e);
        }
        return refs;
    }

    private void extractRefsRecursive(JsonNode node, Set<String> refs) {
        if (node == null) return;
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (key.endsWith("_ref") && value.isTextual()) {
                    refs.add(value.asText());
                }
                extractRefsRecursive(value, refs);
            });
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                extractRefsRecursive(child, refs);
            }
        }
    }
}
