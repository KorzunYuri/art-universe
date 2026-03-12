package yurykorzun.art.universe.common.config.client;

import java.math.BigDecimal;

public enum PropertyType {

    INTEGER {
        @Override
        public Object parse(String value) {
            return Integer.parseInt(value);
        }
    },
    DECIMAL {
        @Override
        public Object parse(String value) {
            return new BigDecimal(value);
        }
    },
    BOOLEAN {
        @Override
        public Object parse(String value) {
            return Boolean.parseBoolean(value);
        }
    },
    STRING {
        @Override
        public Object parse(String value) {
            return value;
        }
    };

    public abstract Object parse(String value);
}
