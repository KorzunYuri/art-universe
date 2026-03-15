package yurykorzun.art.universe.common.config.client.dto;

import lombok.Getter;
import lombok.Setter;
import yurykorzun.art.universe.common.config.client.PropertyType;

@Setter
@Getter
public class RegisterPropertyRequest {

    private String key;
    private PropertyType propertyType;
    private String defaultValue;
    private String description;
    private String constraintsJson;

    public RegisterPropertyRequest() {}

    public RegisterPropertyRequest(String key, PropertyType propertyType, String defaultValue,
                                   String description, String constraintsJson) {
        this.key = key;
        this.propertyType = propertyType;
        this.defaultValue = defaultValue;
        this.description = description;
        this.constraintsJson = constraintsJson;
    }

}
