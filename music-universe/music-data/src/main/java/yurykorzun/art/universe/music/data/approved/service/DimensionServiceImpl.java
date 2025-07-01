package yurykorzun.art.universe.music.data.approved.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.approved.dto.DimensionDto;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.entity.Dimension;
import yurykorzun.art.universe.music.data.approved.repository.DimensionRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DimensionServiceImpl implements DimensionService {

    private final DimensionRepository dimensionRepository;

    public DimensionServiceImpl(DimensionRepository dimensionRepository) {
        this.dimensionRepository = dimensionRepository;
    }

    @Override
    public Page<DimensionDto> searchDimensions(String query, Pageable pageable) {
        Page<Dimension> dimensions = dimensionRepository.searchDimensions(query, pageable);
        
        List<DimensionDto> dimensionDtos = dimensions.getContent().stream()
            .map(dimension -> DimensionDto.builder()
                .id(dimension.getId())
                .name(dimension.getName())
                .build())
            .collect(Collectors.toList());
        
        return new PageImpl<>(dimensionDtos, pageable, dimensions.getTotalElements());
    }

    @Override
    public List<LookupResultDTO> lookupDimensions(String name, Integer limit) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        
        // Apply default limit if null
        int actualLimit = limit != null ? limit : 20;
        
        return dimensionRepository.findByNameContainingIgnoreCase(name.trim(), actualLimit)
            .stream()
            .map(dimension -> LookupResultDTO.builder()
                .id(dimension.getId())
                .name(dimension.getName())
                .build())
            .collect(Collectors.toList());
    }
}
