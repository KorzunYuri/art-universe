package yurykorzun.art.universe.music.data.raw.lastfm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.entity.LastfmTask;

public interface LastfmTaskRepository extends JpaRepository<LastfmTask, Long> {
}
