package yurykorzun.art.universe.music.data.semantic.applicator.applier.service;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.semantic.applicator.repository.MasterDataRepository;

/**
 * Domain service for applying category-related proposals. Hides the
 * find-by-name / create-if-missing dance used by both {@code CREATE_CATEGORY}
 * and {@code BIND_ENTITY_CATEGORY} so strategies can read as a single intent.
 */
@Service
public class CategoryApplicationService {

    private final MasterDataRepository masterDataRepository;

    public CategoryApplicationService(MasterDataRepository masterDataRepository) {
        this.masterDataRepository = masterDataRepository;
    }

    public Resolved findOrCreate(String name) {
        Long existing = masterDataRepository.findCategoryByName(name);
        if (existing != null) {
            return new Resolved(existing, true);
        }
        return new Resolved(masterDataRepository.createCategory(name), false);
    }

    public record Resolved(Long id, boolean existed) {}
}
