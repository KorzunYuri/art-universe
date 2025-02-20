package yurykorzun.art.universe.music.data.raw.lastfm.entity;

import yurykorzun.art.universe.common.persistence.entity.DataCollectionRequest;

import javax.persistence.*;
import java.time.Instant;

@Entity(name = "tags_request")
public class TagsRequest extends DataCollectionRequest {

    public TagsRequest() {
        super();
    }

    public TagsRequest(Instant fromDttm, Instant toDttm) {
        super(fromDttm, toDttm);
    }
}
