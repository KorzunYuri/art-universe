CREATE TABLE config_property
(
        key              VARCHAR(255)                        NOT NULL
    ,   property_type    VARCHAR(20)                         NOT NULL
    ,   current_value    TEXT                                NOT NULL
    ,   default_value    TEXT                                NOT NULL
    ,   description      TEXT
    ,   constraints_json TEXT
    ,   created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    ,   CONSTRAINT config_property_PK PRIMARY KEY (key)
);

CREATE INDEX config_property_IDX_type ON config_property (property_type);
