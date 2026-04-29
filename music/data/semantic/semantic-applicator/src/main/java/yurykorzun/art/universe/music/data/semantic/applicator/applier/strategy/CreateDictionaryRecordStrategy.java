package yurykorzun.art.universe.music.data.semantic.applicator.applier.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ApplicationContext;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalApplyStrategy;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ProposalRow;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.support.ProposalPayloads;
import yurykorzun.art.universe.music.data.semantic.applicator.repository.DictionaryRepository;
import yurykorzun.art.universe.music.data.semantic.model.PayloadFields;
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
        String domain = ProposalPayloads.requireString(payload, PayloadFields.DOMAIN, proposalTypeName());
        String name = ProposalPayloads.requireString(payload, PayloadFields.NAME, proposalTypeName());
        short code;
        if (payload.hasNonNull(PayloadFields.CODE)) {
            code = (short) payload.get(PayloadFields.CODE).asInt();
        } else {
            code = (short) (dictionaryRepository.findMaxCodeForDomain(domain) + 1);
        }
        dictionaryRepository.upsertRecord(domain, code, name);
        return "dictionary:" + domain + ":" + code;
    }
}
