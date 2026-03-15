package yurykorzun.art.universe.common.config.service.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdatePropertyRequest {

    @NotBlank(message = "value must not be blank")
    private String value;

    public UpdatePropertyRequest() {}

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
