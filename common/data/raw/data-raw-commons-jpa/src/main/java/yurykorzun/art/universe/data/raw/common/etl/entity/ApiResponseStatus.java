package yurykorzun.art.universe.data.raw.common.etl.entity;

import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.TransitionAware;

import java.util.Arrays;

public enum ApiResponseStatus implements Coded, TransitionAware<ApiResponseStatus> {

    CREATED(1),
    PENDING(2),
    SCHEDULED(6),
    PROCESSING(3),
    PROCESSING_ERROR(4),
    COMPLETED(5),
    VALIDATION_ERROR(7),
    IS_ERROR_RESPONSE(8);

    private final int code;

    ApiResponseStatus(int code) {
        this.code = code;
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), ApiResponseStatus.class);
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public boolean isValidTransition(ApiResponseStatus to) {
        // TODO add ApiResponseStatus transition validation
        return true;
    }
}
