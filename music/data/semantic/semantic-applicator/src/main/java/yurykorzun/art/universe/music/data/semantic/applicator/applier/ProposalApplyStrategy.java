package yurykorzun.art.universe.music.data.semantic.applicator.applier;

import com.fasterxml.jackson.databind.JsonNode;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

public interface ProposalApplyStrategy {

    ProposalType supportedType();

    String apply(JsonNode payload, ProposalRow proposal, ApplicationContext context);
}
