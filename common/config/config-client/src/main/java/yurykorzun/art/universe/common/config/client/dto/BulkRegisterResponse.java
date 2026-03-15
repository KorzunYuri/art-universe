package yurykorzun.art.universe.common.config.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BulkRegisterResponse {

    private List<PropertyResponse> properties;

    public BulkRegisterResponse() {}

    public BulkRegisterResponse(List<PropertyResponse> properties) {
        this.properties = properties;
    }

}
