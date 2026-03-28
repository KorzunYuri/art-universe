package yurykorzun.art.universe.music.data.semantic.applicator.applier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DependencyResolver {

    private static final Logger log = LoggerFactory.getLogger(DependencyResolver.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ProposalRow> topologicalSort(List<ProposalRow> proposals) {
        // Build adjacency: proposal with ref depends on the proposal with that synth_id
        Map<String, ProposalRow> bySynthId = new HashMap<>();
        for (ProposalRow p : proposals) {
            if (p.getSynthId() != null) {
                bySynthId.put(p.getSynthId(), p);
            }
        }

        Map<Long, Set<Long>> dependencies = new HashMap<>();
        for (ProposalRow p : proposals) {
            dependencies.put(p.getId(), new HashSet<>());
            Set<String> refs = extractRefs(p.getPayload());
            for (String ref : refs) {
                ProposalRow dep = bySynthId.get(ref);
                if (dep != null && !dep.getId().equals(p.getId())) {
                    dependencies.get(p.getId()).add(dep.getId());
                }
            }
        }

        // Kahn's algorithm
        Map<Long, ProposalRow> byId = new HashMap<>();
        for (ProposalRow p : proposals) {
            byId.put(p.getId(), p);
        }

        Map<Long, Integer> inDegree = new HashMap<>();
        for (ProposalRow p : proposals) {
            inDegree.put(p.getId(), 0);
        }
        for (Set<Long> deps : dependencies.values()) {
            for (Long dep : deps) {
                inDegree.merge(dep, 0, Integer::sum); // ensure key exists
            }
        }
        // Recompute: inDegree of X = number of proposals that depend on X? No,
        // inDegree of X = number of proposals X depends on (for topological sort of dependents)
        // Actually for topo sort: edges go from dependency -> dependent
        // So inDegree[dependent] = number of dependencies
        inDegree.clear();
        for (ProposalRow p : proposals) {
            inDegree.put(p.getId(), dependencies.get(p.getId()).size());
        }

        Queue<Long> queue = new LinkedList<>();
        for (var entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<ProposalRow> sorted = new ArrayList<>();
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
            log.warn("Cycle detected in proposal dependencies, {} proposals not sorted", proposals.size() - sorted.size());
            // Add remaining unsorted proposals at the end
            Set<Long> sortedIds = new HashSet<>();
            for (ProposalRow p : sorted) sortedIds.add(p.getId());
            for (ProposalRow p : proposals) {
                if (!sortedIds.contains(p.getId())) sorted.add(p);
            }
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
