package yurykorzun.art.universe.common.config.client.dto;

import lombok.Getter;
import lombok.Setter;
import yurykorzun.art.universe.common.config.client.PropertyType;

@Setter
@Getter
public class PropertyResponse {

    private String key;
    private PropertyType propertyType;
    private String currentValue;
    private String defaultValue;
    private String description;

    public PropertyResponse() {}

}
