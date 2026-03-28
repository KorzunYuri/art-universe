package yurykorzun.art.universe.music.data.semantic.model;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.CodedConverter;

@Converter(autoApply = true)
public class ProposalResolutionConverter extends CodedConverter<ProposalResolution> {

    public ProposalResolutionConverter() {
        super(ProposalResolution.class);
    }
}
