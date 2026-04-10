ALTER TABLE mu_semantic_analysis.analysis_ticket
    ADD CONSTRAINT analysis_ticket_FK_data_source
    FOREIGN KEY (data_source) REFERENCES mu_semantic_analysis.data_source(code);

CREATE INDEX analysis_ticket_I_dedup
    ON mu_semantic_analysis.analysis_ticket (data_source, subject_type, subject_id, analysis_mode);
