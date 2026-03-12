package yurykorzun.art.universe.music.data.raw.spotify.etl.entity;

import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StagingIterationStatusConverter extends CodedConverter<StagingIterationStatus> {

    public StagingIterationStatusConverter() {
        super(StagingIterationStatus.class);
    }
}
