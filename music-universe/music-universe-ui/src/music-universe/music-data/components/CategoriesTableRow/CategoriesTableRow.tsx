// components
import { ReadonlyAttr } from "@/music-universe/shared/components";
// types
import type { Category } from "@/music-universe/music-data/api/music-data-categories";
// styles
import sharedStyles from "@/music-universe/shared/components/EntityTable/EntityTableStyles.module.scss";
import styles from "./CategoriesTableRow.module.css";

interface CategoriesTableRowProps {
    category: Category;
    onChange: (category: Category) => void;
}

export const CategoriesTableRow = ({ category }: CategoriesTableRowProps) => {
    return (
        <div className={sharedStyles.row}>
            <div className={`${sharedStyles.cell} ${styles.name}`}>
                <ReadonlyAttr value={category.name} />
            </div>
            
            <div className={`${sharedStyles.cell} ${styles.parent}`}>
                <ReadonlyAttr value={category.parentName || '-'} />
            </div>
            
            <div className={`${sharedStyles.cell} ${styles.dimension}`}>
                <ReadonlyAttr value={category.dimensionName || '-'} />
            </div>
            
            <div className={`${sharedStyles.cell} ${styles.effectiveDimension}`}>
                <ReadonlyAttr value={category.effectiveDimensionName || '-'} />
            </div>
        </div>
    );
};
