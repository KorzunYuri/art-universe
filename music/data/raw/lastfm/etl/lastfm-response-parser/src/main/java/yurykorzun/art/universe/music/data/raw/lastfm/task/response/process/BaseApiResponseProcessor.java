package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process;

import yurykorzun.art.universe.common.data.raw.etl.entity.ApiCallType;
import yurykorzun.art.universe.common.data.raw.etl.entity.ApiResponse;

import java.io.IOException;

// This class should be moved to a shared 'etl' module if the ETL flow is shared across multiple sources
// TODO convert to an interface?
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
