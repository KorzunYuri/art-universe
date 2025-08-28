package yurykorzun.art.universe.music.quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class GameDto {
    private Long id;
    private Long generationId;
    private Instant createdAt;
}
