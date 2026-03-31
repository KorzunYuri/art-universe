package yurykorzun.art.universe.music.data.semantic.model;

public final class AnalysisVersions {

    public static final String UNIFIED_V1 = "unified-1.0.0";
    public static final String CATEGORY_NAME_V1 = "category-name-1.0.0";

    public static final String CURRENT_UNIFIED = UNIFIED_V1;
    public static final String CURRENT_CATEGORY_NAME = CATEGORY_NAME_V1;

    public static String currentVersionFor(AnalysisMode mode) {
        return switch (mode) {
            case FULL_EXTRACTION -> CURRENT_UNIFIED;
            case CREATIVE_CATEGORIZATION -> CURRENT_CATEGORY_NAME;
        };
    }

    private AnalysisVersions() {
    }
}
