package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;

import java.util.List;

import static yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants.HIBERNATE_BATCH_SIZE;

@NoRepositoryBean
public interface BaseLastfmApiResponseRepository extends JpaRepository<LastfmApiResponse, Long> {
}
