// components
import { ReadonlyAttr } from "@/music-universe/shared/components";
// types
import type { Dimension } from "@/music-universe/music-data/api/music-data-dimensions";
// styles
import sharedStyles from "@/music-universe/shared/components/EntityTable/EntityTableStyles.module.scss";
import styles from "./DimensionsTableRow.module.css";

interface DimensionsTableRowProps {
    dimension: Dimension;
    onChange: (dimension: Dimension) => void;
}

export const DimensionsTableRow = ({ dimension }: DimensionsTableRowProps) => {
    return (
        <div className={sharedStyles.row}>
            <div className={`${sharedStyles.cell} ${styles.name}`}>
                <ReadonlyAttr value={dimension.name} />
            </div>
        </div>
    );
};
