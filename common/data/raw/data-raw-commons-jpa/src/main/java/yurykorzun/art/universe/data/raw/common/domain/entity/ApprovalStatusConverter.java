package yurykorzun.art.universe.data.raw.common.domain.entity;

import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

public class ApprovalStatusConverter extends CodedConverter<ApprovalStatus> {

    public ApprovalStatusConverter() {
        super(ApprovalStatus.class);
    }
}
