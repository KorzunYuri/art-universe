-- NOTIFY triggers for semantic analysis pipeline event-driven scheduling.
-- Each stage listens for notifications from the previous stage.

-- Notify semantic-analyzer when new tickets are inserted
CREATE OR REPLACE FUNCTION mu_semantic_analysis.notify_tickets_ready()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM pg_notify('semantic_tickets_ready', '');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_notify_tickets_ready
    AFTER INSERT ON mu_semantic_analysis.analysis_ticket
    FOR EACH STATEMENT
    EXECUTE FUNCTION mu_semantic_analysis.notify_tickets_ready();

-- Notify semantic-response-parser when analysis requests are completed
CREATE OR REPLACE FUNCTION mu_semantic_analysis.notify_analysis_completed()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 3 THEN -- COMPLETED
        PERFORM pg_notify('semantic_analysis_completed', '');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_notify_analysis_completed
    AFTER UPDATE OF status ON mu_semantic_analysis.analysis_request
    FOR EACH ROW
    EXECUTE FUNCTION mu_semantic_analysis.notify_analysis_completed();

-- Notify semantic-applicator when new proposals are inserted
CREATE OR REPLACE FUNCTION mu_semantic_analysis.notify_proposals_ready()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM pg_notify('semantic_proposals_ready', '');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_notify_proposals_ready
    AFTER INSERT ON mu_semantic_analysis.proposal
    FOR EACH STATEMENT
    EXECUTE FUNCTION mu_semantic_analysis.notify_proposals_ready();
