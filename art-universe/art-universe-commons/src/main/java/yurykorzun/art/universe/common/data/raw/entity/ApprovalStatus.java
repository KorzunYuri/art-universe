package yurykorzun.art.universe.common.data.raw.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.TransitionAware;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;

import java.util.Arrays;

@Getter
public enum ApprovalStatus implements Coded, TransitionAware<ApiCallStatus> {

    PENDING(1),
    APPROVED(2),
    DECLINED(3),
    PRE_APPROVED(4),
    IGNORED(5);

    private final int code;

    ApprovalStatus(int code) {
        this.code = code;
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), ApprovalStatus.class);
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public boolean isValidTransition(ApiCallStatus to) {
        // TODO add ApprovalStatus transition validation
        return true;
    }
}
