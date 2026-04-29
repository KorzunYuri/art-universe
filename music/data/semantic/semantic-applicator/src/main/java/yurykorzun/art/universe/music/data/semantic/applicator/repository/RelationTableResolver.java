package yurykorzun.art.universe.music.data.semantic.applicator.repository;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import java.util.EnumSet;
import java.util.Set;

@Component
public class RelationTableResolver {

    public record Resolution(
        String table,
        String firstColumn,
        String secondColumn,
        Long firstId,
        Long secondId
    ) {}

    private static final Set<MasterEntityType> KNOWN_TYPES = EnumSet.of(
        MasterEntityType.ARTIST,
        MasterEntityType.ALBUM,
        MasterEntityType.TRACK,
        MasterEntityType.CATEGORY,
        MasterEntityType.PERSON
    );

    public Resolution resolve(MasterEntityType sourceType, Long sourceId, MasterEntityType targetType, Long targetId) {
        ensureKnown(sourceType);
        ensureKnown(targetType);

        if (sourceType == targetType) {
            String table = sourceType.getName() + "_" + sourceType.getName();
            return new Resolution(
                table,
                "source_" + sourceType.getName() + "_id",
                "target_" + sourceType.getName() + "_id",
                sourceId,
                targetId
            );
        }

        MasterEntityType first;
        MasterEntityType second;
        Long firstId;
        Long secondId;
        if (physicalOrder(sourceType) <= physicalOrder(targetType)) {
            first = sourceType;
            second = targetType;
            firstId = sourceId;
            secondId = targetId;
        } else {
            first = targetType;
            second = sourceType;
            firstId = targetId;
            secondId = sourceId;
        }

        String table = first.getName() + "_" + second.getName();
        return new Resolution(
            table,
            first.getName() + "_id",
            second.getName() + "_id",
            firstId,
            secondId
        );
    }

    private int physicalOrder(MasterEntityType type) {
        return switch (type) {
            case ARTIST -> 1;
            case ALBUM -> 2;
            case TRACK -> 3;
            case PERSON -> 4;
            case CATEGORY -> 5;
        };
    }

    private void ensureKnown(MasterEntityType type) {
        if (!KNOWN_TYPES.contains(type)) {
            throw new IllegalArgumentException("Unsupported entity type for relation: " + type);
        }
    }
}
