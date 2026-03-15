package yurykorzun.art.universe.common.config.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BulkRegisterRequest {

    private List<RegisterPropertyRequest> properties;

    public BulkRegisterRequest() {}

    public BulkRegisterRequest(List<RegisterPropertyRequest> properties) {
        this.properties = properties;
    }

}
