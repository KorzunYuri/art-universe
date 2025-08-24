package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;

/**
 * Is used to maintain consistency of {@link LastfmAttribute}
 * values between code and database
 */
public interface LastfmAttributeEntityRepository extends JpaRepository<LastfmAttributeEntity, Integer> {
}
