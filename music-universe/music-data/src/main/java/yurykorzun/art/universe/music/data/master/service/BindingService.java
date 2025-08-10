package yurykorzun.art.universe.music.data.master.service;

import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindResponseDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.EntityType;

/**
 * Universal service for entity binding operations
 */
public interface BindingService {
    
    /**
     * Batch unbind external entities from master entities
     * 
     * @param entityType Entity type to unbind
     * @param dataSource Data source of external entities
     * @param request Request containing list of external IDs to unbind
     * @return Response with unbinding results
     */
    BatchUnbindResponseDTO batchUnbind(EntityType entityType, DataSource dataSource, BatchUnbindRequestDTO request);
}
