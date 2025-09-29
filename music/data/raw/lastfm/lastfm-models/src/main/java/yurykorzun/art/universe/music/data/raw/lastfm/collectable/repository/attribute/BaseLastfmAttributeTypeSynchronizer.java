package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Updates all values of {@link LastfmAttribute} in DB on startup.
 */
@Component
public class BaseLastfmAttributeTypeSynchronizer {

    private final BaseLastfmAttributeEntityRepository attributeRepository;

    public BaseLastfmAttributeTypeSynchronizer(BaseLastfmAttributeEntityRepository attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    @PostConstruct
    public void syncAttributes() {
        List<LastfmAttributeEntity> entitiesToSave = new ArrayList<>();
        for (LastfmAttribute attribute : LastfmAttribute.values()) {
            LastfmAttributeEntity entity = attributeRepository.findById(attribute.getId())
                    .orElseGet(LastfmAttributeEntity::new);
            entity.setId(attribute.getId());
            entity.setName(attribute.getName());
            entity.setDescription(attribute.getDescription());
            entity.setType(attribute.getDataType().getCode());
            entity.setUpdatedAt(Instant.now());
            entitiesToSave.add(entity);
        }
        attributeRepository.saveAll(entitiesToSave);
    }
}
