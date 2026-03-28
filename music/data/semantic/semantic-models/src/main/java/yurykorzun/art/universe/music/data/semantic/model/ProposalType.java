package yurykorzun.art.universe.music.data.semantic.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum ProposalType implements Coded {
    CREATE_ENTITY(1, "create_entity"),
    CREATE_RELATION(2, "create_relation"),
    CREATE_ATTRIBUTE(3, "create_attribute"),
    CREATE_ATTRIBUTE_DEF(4, "create_attribute_def"),
    MODIFY_ATTRIBUTE(5, "modify_attribute"),
    BIND_ENTITY_CATEGORY(6, "bind_entity_category"),
    CREATE_CATEGORY(7, "create_category"),
    CREATE_DICTIONARY_RECORD(8, "create_dictionary_record"),
    BIND_EXTERNAL_ENTITY(9, "bind_external_entity");

    private final int code;
    private final String name;

    ProposalType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    @JsonValue
    public String getName() {
        return name;
    }

    @JsonCreator
    public static ProposalType fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Proposal type name cannot be null");
        }
        for (ProposalType type : ProposalType.values()) {
            if (type.name.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown proposal type: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), ProposalType.class);
    }
}
