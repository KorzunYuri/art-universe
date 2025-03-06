package yurykorzun.art.universe.music.data.raw.lastfm.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.task.entity.LastfmTask;

public interface LastfmTaskRepository extends JpaRepository<LastfmTask, Long> {
}
