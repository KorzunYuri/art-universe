package yurykorzun.art.universe.music.data.semantic.applicator.applier.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ApplicationContext;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalApplyStrategy;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.support.ProposalPayloads;
import yurykorzun.art.universe.music.data.semantic.applicator.repository.DictionaryRepository;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

@Component
public class CreateDictionaryRecordStrategy implements ProposalApplyStrategy {

    private final DictionaryRepository dictionaryRepository;

    public CreateDictionaryRecordStrategy(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    @Override
    public ProposalType supportedType() {
        return ProposalType.CREATE_DICTIONARY_RECORD;
    }

    @Override
    public String apply(JsonNode payload, ProposalRow proposal, ApplicationContext context) {
        String domain = ProposalPayloads.requireString(payload, "domain", proposalTypeName());
        String name = ProposalPayloads.requireString(payload, "name", proposalTypeName());
        short code;
        if (payload.hasNonNull("code")) {
            code = (short) payload.get("code").asInt();
        } else {
            code = (short) (dictionaryRepository.findMaxCodeForDomain(domain) + 1);
        }
        dictionaryRepository.upsertRecord(domain, code, name);
        return "dictionary:" + domain + ":" + code;
    }
}
