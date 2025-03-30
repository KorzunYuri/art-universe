package yurykorzun.art.universe.common.data.raw.api.methods.common;

import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponse;

import java.io.IOException;

public abstract class BaseApiResponseProcessor <T extends ApiResponse> {

    protected abstract ApiCallType getApiCallType(); // rename for clarity
    protected abstract void processResponse(T response) throws IOException;

    public final void process(T response) throws IOException {
        validateResponse(response);
        processResponse(response);
    }

    protected void validateResponse(T response) {
        if (!this.getApiCallType().equals(response.getApiCall().getType())) {
            throw new IllegalArgumentException(
                    String.format("Response of type %s doesn't match processor's type %s", response.getApiCall().getType(), this.getApiCallType()));
        }
    }

}
