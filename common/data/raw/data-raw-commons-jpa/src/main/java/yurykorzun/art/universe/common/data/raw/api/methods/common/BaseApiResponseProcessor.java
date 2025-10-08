package yurykorzun.art.universe.common.data.raw.api.methods.common;

import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponse;

import java.io.IOException;

// should probably be moved to another module in the future
public abstract class BaseApiResponseProcessor <T extends ApiResponse> {

    public abstract ApiCallType getApiCallType();

    protected abstract void processResponse(T sourceApiResponse) throws IOException;

    public void process(T sourceApiResponse) throws IOException {
        validateResponse(sourceApiResponse);
        processResponse(sourceApiResponse);
    }

    protected void validateResponse(T sourceApiResponse) {
        if (!this.getApiCallType().equals(sourceApiResponse.getApiCall().getType())) {
            throw new IllegalArgumentException(
                    String.format("Response of type %s doesn't match processor's type %s", sourceApiResponse.getApiCall().getType(), this.getApiCallType()));
        }
    }

}
