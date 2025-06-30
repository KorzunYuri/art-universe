package yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.dto.ArtistGetSimilarDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.dto.ArtistSearchDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.dto.ArtistTopAlbumsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.dto.ArtistTopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.DtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.TagTopArtistsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TagTopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

import javax.annotation.Nullable;
import java.util.*;

import static yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants.*;

@Getter
public enum LastfmApiCallType implements ApiCallType {

        TAG_TOP_TAGS(
            1,
            "tag.getTopTags",
            Set.of(PARAM_NAME_API_KEY),
            Set.of(PARAM_NAME_OFFSET),
            TagTopTagsDtoRoot.class,
            null)
    ,   TAG_TOP_ARTISTS(
            2,
            "tag.getTopArtists",
            Set.of(PARAM_NAME_API_KEY, PARAM_NAME_TAG),
            Set.of(PARAM_NAME_LIMIT, PARAM_NAME_PAGE),
            TagTopArtistsDtoRoot.class,
            LastfmTag.class)
    ,   TAG_TOP_TRACKS(
            3,
            "tag.getTopTracks",
            Set.of(PARAM_NAME_API_KEY, PARAM_NAME_TAG),
            Set.of(PARAM_NAME_LIMIT, PARAM_NAME_PAGE),
            TagTopTracksDtoRoot.class,
            LastfmTag.class)
    ,   ARTIST_GET_INFO(
            4,
            "artist.getInfo",
            Set.of(PARAM_NAME_API_KEY),
            Set.of(PARAM_NAME_ARTIST, PARAM_NAME_MBID),
            ArtistGetInfoDtoRoot.class,
            null)
    ,   ARTIST_TOP_TAGS(
            5,
            "artist.getTopTags",
            Set.of(PARAM_NAME_API_KEY),
            Set.of(PARAM_NAME_ARTIST, PARAM_NAME_MBID, PARAM_NAME_AUTOCORRECT, PARAM_NAME_LIMIT),
            ArtistTopTagsDtoRoot.class,
            LastfmArtist.class)
    ,   ARTIST_TOP_TRACKS(
            6,
            "artist.getTopTracks",
            Set.of(PARAM_NAME_API_KEY),
            Set.of(PARAM_NAME_ARTIST, PARAM_NAME_MBID, PARAM_NAME_AUTOCORRECT, PARAM_NAME_LIMIT),
            ArtistTopTracksDtoRoot.class,
            LastfmArtist.class)
    ,   ARTIST_TOP_ALBUMS(
            7,
            "artist.getTopAlbums",
            Set.of(PARAM_NAME_API_KEY),
            Set.of(PARAM_NAME_ARTIST, PARAM_NAME_MBID, PARAM_NAME_AUTOCORRECT, PARAM_NAME_LIMIT),
            ArtistTopAlbumsDtoRoot.class,
            LastfmArtist.class)
    ,   ARTIST_GET_SIMILAR(
            8,
            "artist.getSimilar",
            Set.of(PARAM_NAME_API_KEY),
            Set.of(PARAM_NAME_ARTIST, PARAM_NAME_MBID, PARAM_NAME_AUTOCORRECT, PARAM_NAME_LIMIT),
            ArtistGetSimilarDtoRoot.class,
            LastfmArtist.class)
    ,   ARTIST_SEARCH(
            9,
            "artist.search",
            Set.of(PARAM_NAME_API_KEY, PARAM_NAME_ARTIST),
            Set.of(PARAM_NAME_LIMIT),
            ArtistSearchDtoRoot.class,
            null
        );

    static {
        CodedRegistry.register(Arrays.asList(values()), ApiCallType.class);
    }

    private final int code;
    private final String method;
    private final Map<String, String> defaultParameterValues = new HashMap<>();
    private final Collection<String> mandatoryParameters;
    private final Collection<String> optionalParameters;
    private final Class<? extends DtoRoot> responseDtoClass;
    @Nullable
    private final Class<? extends BaseLastfmEntity> scopeEntityType;

    LastfmApiCallType(
            int code,
            String method,
            Collection<String> mandatoryParameters,
            Collection<String> optionalParameters,
            Class<? extends DtoRoot> responseDtoClass,
            Class<? extends BaseLastfmEntity> scopeEntityType
    ) {
        this.code = code;
        this.method = method;
        this.optionalParameters = optionalParameters;
        this.mandatoryParameters = Set.copyOf(mandatoryParameters);
        this.responseDtoClass = responseDtoClass;
        this.scopeEntityType = scopeEntityType;

        this.defaultParameterValues.put(PARAM_NAME_METHOD, method);
        this.defaultParameterValues.put(PARAM_NAME_FORMAT, PARAM_DEFAULT_FORMAT);
    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public Map<String, String> getDefaultParamValues() {
        return Collections.unmodifiableMap(this.defaultParameterValues);
    }

    @Override
    public Collection<String> getMandatoryParams() {
        return Set.copyOf(this.mandatoryParameters);
    }

    @Override
    public Collection<String> getOptionalParams() {
        return Set.copyOf(this.optionalParameters);
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return name();
    }
}
