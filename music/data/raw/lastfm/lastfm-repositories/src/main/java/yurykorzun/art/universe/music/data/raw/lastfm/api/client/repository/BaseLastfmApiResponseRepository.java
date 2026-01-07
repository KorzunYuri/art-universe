package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;

@NoRepositoryBean
public interface BaseLastfmApiResponseRepository extends JpaRepository<LastfmApiResponse, Long> {
}
