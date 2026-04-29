package yurykorzun.art.universe.music.data.semantic.model;

import lombok.Getter;

@Getter
public enum PayloadFieldType {
    STRING("string"),
    INTEGER("integer"),
    DATE("date"),
    ARRAY("array");

    private final String value;

    PayloadFieldType(String value) {
        this.value = value;
    }

}
