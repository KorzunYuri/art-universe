package yurykorzun.art.universe.music.data.master.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum MasterApprovalStatus implements Coded {
    NEW(0, "new"),
    PENDING(1, "pending"),
    APPROVED(2, "approved"),
    REJECTED(3, "rejected");

    private final int code;
    private final String name;

    MasterApprovalStatus(int code, String name) {
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
    public static MasterApprovalStatus fromString(String name) {
        if (name == null) {
            throw new IllegalArgumentException("MasterApprovalStatus name cannot be null");
        }
        for (MasterApprovalStatus status : MasterApprovalStatus.values()) {
            if (status.name.equalsIgnoreCase(name) || status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown approval status: " + name);
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), MasterApprovalStatus.class);
    }
}
