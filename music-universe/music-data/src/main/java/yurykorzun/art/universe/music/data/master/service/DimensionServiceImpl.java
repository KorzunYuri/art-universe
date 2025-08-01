package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.master.dto.DimensionDto;
import yurykorzun.art.universe.music.data.master.dto.DimensionSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.Dimension;
import yurykorzun.art.universe.music.data.master.entity.EntityType;
import yurykorzun.art.universe.music.data.master.repository.DimensionRepository;
import yurykorzun.art.universe.music.data.master.service.lookup.DimensionLookupService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DimensionServiceImpl implements DimensionService {

    private final DimensionRepository dimensionRepository;
    private final DimensionLookupService lookupService;

    public DimensionServiceImpl(DimensionRepository dimensionRepository, EntityManager entityManager) {
        this.dimensionRepository = dimensionRepository;
        this.lookupService = new DimensionLookupService(entityManager, EntityType.DIMENSION);
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
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search(name)
            .limit(limit)
            .build();
        
        return lookupService.lookup(request);
    }

    @Override
    @Transactional
    public DimensionDto saveDimension(DimensionSaveRequestDTO request) {
        Dimension dimension;
        
        if (request.getId() != null) {
            // Update existing dimension
            dimension = dimensionRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Dimension not found with id: " + request.getId()));
            
            dimension.setName(request.getName());
        } else {
            // Create new dimension - check for duplicate name
            Optional<Dimension> existingDimension = dimensionRepository.findByNameIgnoreCase(request.getName());
            if (existingDimension.isPresent()) {
                throw new IllegalArgumentException("Dimension with name '" + request.getName() + "' already exists");
            }
            
            dimension = Dimension.builder()
                .name(request.getName())
                .build();
        }
        
        Dimension savedDimension = dimensionRepository.save(dimension);
        
        return DimensionDto.builder()
            .id(savedDimension.getId())
            .name(savedDimension.getName())
            .build();
    }

    @Override
    @Transactional
    public boolean deleteDimension(Long id) {
        if (dimensionRepository.existsById(id)) {
            dimensionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
