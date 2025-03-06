package yurykorzun.art.universe.music.data.raw.lastfm.apiclient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.entity.LastfmApiCall;

@Repository
public interface LastfmApiCallRepository extends JpaRepository<LastfmApiCall, Long> {
}
