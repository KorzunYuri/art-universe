package yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

@Getter
public enum TextContentType implements Coded {

    BIO_SUMMARY(1, "BIO_SUMMARY"),
    BIO_CONTENT(2, "BIO_CONTENT"),
    WIKI_SUMMARY(3, "WIKI_SUMMARY"),
    WIKI_CONTENT(4, "WIKI_CONTENT");

    private final int code;
    private final String name;

    TextContentType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), TextContentType.class);
    }
}
