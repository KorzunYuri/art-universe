package yurykorzun.art.universe.music.data.approved.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for passing a pair of entity identifiers in a relation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationPair {
    private Long sourceId;
    private Long targetId;
    
    /**
     * Creates a RelationPair object from a string in the format "sourceId-targetId"
     * 
     * @param pair String in the format "sourceId-targetId"
     * @return RelationPair object
     * @throws IllegalArgumentException if the string format is invalid
     */
    public static RelationPair fromString(String pair) {
        String[] parts = pair.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid relation pair format: " + pair);
        }
        try {
            return new RelationPair(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid relation pair format: " + pair, e);
        }
    }
}
