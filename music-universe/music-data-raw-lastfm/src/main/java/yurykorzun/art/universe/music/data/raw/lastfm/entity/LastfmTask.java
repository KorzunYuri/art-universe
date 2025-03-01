package yurykorzun.art.universe.music.data.raw.lastfm.entity;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.DataCollectionTask;

import javax.persistence.*;

@Entity(name = "task")
@SuperBuilder
@NoArgsConstructor
public class LastfmTask extends DataCollectionTask {
}
