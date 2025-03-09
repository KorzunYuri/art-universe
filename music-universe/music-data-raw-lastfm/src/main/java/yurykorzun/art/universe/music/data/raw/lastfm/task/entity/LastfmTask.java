package yurykorzun.art.universe.music.data.raw.lastfm.task.entity;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.task.entity.Task;

import jakarta.persistence.*;

@Entity(name = "task")
@SuperBuilder
@NoArgsConstructor
public class LastfmTask extends Task {
}
