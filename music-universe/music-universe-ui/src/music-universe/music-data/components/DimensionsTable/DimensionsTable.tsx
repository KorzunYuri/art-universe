// types
import type { Dimension } from '@/music-universe/shared/types/entities.ts'
// api
import { fetchDimensions } from '@/music-universe/music-data/api/music-data-dimensions'
// components
import { MasterEntityTable } from '@/music-universe/shared/components'
import { DimensionsTableHeader } from '../DimensionsTableHeader'
import { DimensionsTableRow } from '../DimensionsTableRow'
// styles
import styles from './DimensionsTable.module.css'

export const DimensionsTable = () => {
    return (
        <div className={styles.container}>
            <MasterEntityTable<Dimension>
                fetchEntities={fetchDimensions}
                renderHeader={(sort, setSort) => (
                    <DimensionsTableHeader sort={sort} setSort={setSort} />
                )}
                renderRow={(dimension) => (
                    <DimensionsTableRow 
                        key={dimension.id} 
                        entity={dimension}
                    />
                )}
                searchPlaceholder="Search dimension name..."
            />
        </div>
    )
}
