package yurykorzun.art.universe.music.data.raw.lastfm.task.service;

import yurykorzun.art.universe.common.persistence.entity.TaskStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.task.dto.LastfmTaskCreateRequest;

public interface LastfmTaskService {

    long createRequest(LastfmTaskCreateRequest dto);

    void setStatus(long id, TaskStatus status) throws IllegalStateException;

}
