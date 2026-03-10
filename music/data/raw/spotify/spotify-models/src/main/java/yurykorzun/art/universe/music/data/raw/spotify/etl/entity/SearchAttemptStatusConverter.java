package yurykorzun.art.universe.music.data.raw.spotify.etl.entity;

import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SearchAttemptStatusConverter extends CodedConverter<SearchAttemptStatus> {

    public SearchAttemptStatusConverter() {
        super(SearchAttemptStatus.class);
    }
}
