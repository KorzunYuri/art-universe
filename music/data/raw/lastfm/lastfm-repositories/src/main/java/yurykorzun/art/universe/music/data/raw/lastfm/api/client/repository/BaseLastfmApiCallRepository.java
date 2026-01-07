package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;

@NoRepositoryBean
public interface BaseLastfmApiCallRepository extends JpaRepository<LastfmApiCall, Long> {
}
