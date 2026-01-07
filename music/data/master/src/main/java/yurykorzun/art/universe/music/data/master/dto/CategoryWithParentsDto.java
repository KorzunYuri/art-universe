package yurykorzun.art.universe.music.data.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithParentsDto {
    private Long id;
    private String name;
    private List<CategoryDto> parents;
}
