package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@Getter
public enum LastfmAttribute implements Coded {

        RELATIONS_COUNT(
            1,
            "Relations number",
            "Number of entities associated with entity",
            DataType.INTEGER,
            HistoryType.SNAPSHOT,
            List.of(LastfmEntityType.TAG))
    ,   REACH(
            2,
            "Reach",
            "Number of users that used entity",
            DataType.INTEGER,
            HistoryType.SNAPSHOT,
            List.of(LastfmEntityType.TAG))
    ,   URL(
            3,
            "URL",
            "Entity's URL",
            DataType.STRING,
            HistoryType.SCD2,
            List.of(LastfmEntityType.values()))
    ,   RANK(
            4,
            "rank",
            "Rank of the entity, global or scoped",
            DataType.INTEGER,
            HistoryType.SNAPSHOT,
            List.of(LastfmEntityType.values()))
    ,   MBID(
            5,
            "mbid",
            "MusicBrainz ID",
            DataType.STRING,
            HistoryType.SCD2,
            List.of(LastfmEntityType.ARTIST))
    ;

    private final int id;
    private final String name;
    private final String description;
    private final DataType dataType;
    private final HistoryType historyType;
    private final Collection<LastfmEntityType> supportedEntities;

    static {
        CodedRegistry.register(Arrays.asList(values()), LastfmAttribute.class);
    }

    LastfmAttribute(int id, String name, String description,
                    DataType dataType, HistoryType historyType,
                    Collection<LastfmEntityType> supportedEntities) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dataType = dataType;
        this.historyType = historyType;
        this.supportedEntities = supportedEntities;
    }

    @Override
    public Integer getCode() {
        return id;
    }

    @AllArgsConstructor
    public enum DataType implements Coded {
            STRING(1)
        ,   INTEGER(2)
        ;

        private final int id;

        @Override
        public Integer getCode() {
            return id;
        }
    }

    public enum HistoryType {
        /**
         * Add new attribute value record only if the value has changed
         */
        SCD2, // SlowlyChangingDimension
        /**
         * Add new attribute value record with every snapshot
         */
        SNAPSHOT
    }
}
