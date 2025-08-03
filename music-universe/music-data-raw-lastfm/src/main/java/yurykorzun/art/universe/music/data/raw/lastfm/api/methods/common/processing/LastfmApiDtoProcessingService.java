package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

import java.util.List;

@Service
public class LastfmApiDtoProcessingService {

    private final LastfmAttributeHistoryService attributeHistoryService;

    public LastfmApiDtoProcessingService(
        LastfmAttributeHistoryService attributeHistoryService
    ) {
        this.attributeHistoryService = attributeHistoryService;
    }

    public <E extends BaseLastfmEntity, D extends EntityDto<E>> LastfmApiDtoProcessingResult<E, D> process(
        LastfmApiCall sourceApiCall,
        List<D> dtos,
        EntityFactory<E, D> entityFactory,
        List<EntityAttributeHandler<E, ?, D>> attrHandlers,
        EntityService<E> entityService
    ) {
        LastfmApiDtoProcessor<E, D> processor = new LastfmApiDtoProcessor<>();

        List<E> entitiesByUniqueKeys = entityService.findExistingEntities(dtos);
        return processor.process(
            dtos,
            entitiesByUniqueKeys,
            sourceApiCall,
            entityFactory,
            attrHandlers,
            entityService::saveAll,
            attributeHistoryService::upsertCandidateValues
        );
    }
}
