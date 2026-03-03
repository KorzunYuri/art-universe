package yurykorzun.art.universe.music.data.master.service.lookup;

import jakarta.persistence.EntityManager;
import yurykorzun.art.universe.common.domain.service.lookup.BaseLookupService;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityMetadata;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

/**
 * Type-safe lookup service for master entities that extends parametrized BaseLookupService
 */
public class MasterEntityLookupService extends BaseLookupService<MasterEntityType, MasterEntityMetadata> {

    public MasterEntityLookupService(EntityManager entityManager, MasterEntityType entityType) {
        super(entityManager, entityType);
    }

    @Override
    protected MasterEntityMetadata createEntityMetadata() {
        return new MasterEntityMetadata(getEntityType());
    }
}
