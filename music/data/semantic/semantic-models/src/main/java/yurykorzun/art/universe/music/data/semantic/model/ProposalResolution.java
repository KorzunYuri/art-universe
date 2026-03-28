package yurykorzun.art.universe.music.data.semantic.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum ProposalResolution implements Coded {
    PENDING(1, "pending"),
    APPROVED(2, "approved"),
    DECLINED(3, "declined"),
    SUPERSEDED(4, "superseded"),
    AUTO_APPROVED(5, "auto_approved"),
    AUTO_DECLINED(6, "auto_declined");

    private final int code;
    private final String name;

    ProposalResolution(int code, String name) {
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
    public static ProposalResolution fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Proposal resolution name cannot be null");
        }
        for (ProposalResolution resolution : ProposalResolution.values()) {
            if (resolution.name.equalsIgnoreCase(name) || resolution.name().equalsIgnoreCase(name)) {
                return resolution;
            }
        }
        throw new IllegalArgumentException("Unknown proposal resolution: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), ProposalResolution.class);
    }
}
