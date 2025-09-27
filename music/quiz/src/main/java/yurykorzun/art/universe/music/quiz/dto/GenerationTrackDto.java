package yurykorzun.art.universe.music.quiz.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenerationTrackDto {
    private Long trackId;
    private String trackName;
    private String artistName;
    private Integer orderIndex;
}
