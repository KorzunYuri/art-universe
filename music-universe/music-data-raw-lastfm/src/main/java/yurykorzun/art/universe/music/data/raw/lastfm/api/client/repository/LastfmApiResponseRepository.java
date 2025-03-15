package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;

@Repository
public interface LastfmApiResponseRepository extends JpaRepository<LastfmApiResponse, Long> {
}
