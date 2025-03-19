package yurykorzun.art.universe.music.data.raw.lastfm.task.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.task.entity.Task;

import jakarta.persistence.*;

@Entity(name = "task")
@SuperBuilder
@NoArgsConstructor
@Getter
public class LastfmTask extends Task {

    @Id
    @SequenceGenerator(
            name = "task_seq_gen",
            sequenceName = "task_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_seq_gen")
    private long id;

}
