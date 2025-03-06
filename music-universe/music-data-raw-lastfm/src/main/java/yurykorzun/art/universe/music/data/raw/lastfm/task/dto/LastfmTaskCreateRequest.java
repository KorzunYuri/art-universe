package yurykorzun.art.universe.music.data.raw.lastfm.task.dto;

import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.task.dto.TaskCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.task.entity.LastfmTaskType;

@SuperBuilder
public class LastfmTaskCreateRequest extends TaskCreateRequest<LastfmTaskType> {
}
