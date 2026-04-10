package yurykorzun.art.universe.music.data.master.model;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

@Converter(autoApply = true)
public class MasterApprovalStatusConverter extends CodedConverter<MasterApprovalStatus> {

    public MasterApprovalStatusConverter() {
        super(MasterApprovalStatus.class);
    }
}
