package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeEntity;

/**
 * Is used to maintain consistency of {@link yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute}
 * values between code and database
 */
public interface LastfmAttributeEntityRepository extends JpaRepository<LastfmAttributeEntity, Integer> {
}
