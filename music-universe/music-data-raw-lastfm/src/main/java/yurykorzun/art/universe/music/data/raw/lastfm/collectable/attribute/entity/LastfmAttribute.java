package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import java.util.Collection;
import java.util.List;


@Getter
public enum LastfmAttribute implements Coded {

        RELATIONS_COUNT(
            1,
            "Relations number",
            "Number of entities associated with entity",
            Type.INTEGER,
            List.of(LastfmEntityType.TAG))
    ,   REACH(
            2,
            "Reach",
            "Number of users that used entity",
            Type.INTEGER,
            List.of(LastfmEntityType.TAG))
    ,   URL(
            3,
            "URL",
            "Entity's URL",
            Type.STRING,
            List.of(LastfmEntityType.values()))
    ,   RANK(
            4,
            "rank",
            "global rank of the entity",
            Type.INTEGER,
            List.of(LastfmEntityType.values()))
    ;

    private final int id;
    private final String name;
    private final String description;
    private final LastfmAttribute.Type type;
    private final Collection<LastfmEntityType> supportedEntities;

    LastfmAttribute(int id, String name, String description, Type type,
                    Collection<LastfmEntityType> supportedEntities) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.supportedEntities = supportedEntities;
    }

    @Override
    public Integer getCode() {
        return id;
    }

    @AllArgsConstructor
    public enum Type implements Coded {
            STRING(1)
        ,   INTEGER(2)
        ;

        private final int id;

        @Override
        public Integer getCode() {
            return id;
        }
    }
}
