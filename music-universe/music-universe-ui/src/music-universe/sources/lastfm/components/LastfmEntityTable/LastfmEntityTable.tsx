// hooks
import { useEffect } from 'react'
import type { ReactNode } from 'react'
// types
import type { LastfmEntity } from '@/music-universe/sources/lastfm/types/lastfm-entity'
import type { MasterEntity } from '@/music-universe/shared/types/entities.ts'
import type { Page } from '@/music-universe/shared/types/page'
import type { BoundEntityResponse } from '@/music-universe/music-data/utils/master-entities-common.ts'
// components
import { RawEntityTable } from '@/music-universe/shared/components'
import type {SearchParams} from "@/music-universe/shared/components/BaseEntityTable/BaseEntityTable.tsx";

interface LastfmEntityTableProps<T extends LastfmEntity<M>, M extends MasterEntity> {
    fetchEntities: (params: SearchParams) => Promise<Page<T>>
    fetchMasterEntities: (externalIds: number[]) => Promise<BoundEntityResponse[]>
    renderHeader: (sort: string, setSort: (sort: string) => void) => ReactNode
    renderRow: (entity: T) => ReactNode
    searchPlaceholder?: string
    createMasterEntity: (masterEntityData: BoundEntityResponse) => M
    pageSize?: number
}

export function LastfmEntityTable<T extends LastfmEntity<M>, M extends MasterEntity>({
    fetchEntities,
    fetchMasterEntities,
    renderHeader,
    renderRow,
    searchPlaceholder = "Search...",
    createMasterEntity,
    pageSize = 20
}: LastfmEntityTableProps<T, M>) {
    
    // Mount/unmount logging
    useEffect(() => {
        console.log('🔧 LastfmEntityTable MOUNTED with props:', {
            searchPlaceholder,
            pageSize
        });
        return () => {
            console.log('🔧 LastfmEntityTable UNMOUNTED');
        };
    }, []);

    // Render logging
    console.log('🔧 LastfmEntityTable RENDER with props:', {
        searchPlaceholder,
        pageSize
    });

    return (
        <RawEntityTable<T, M>
            fetchEntities={fetchEntities}
            fetchMasterEntities={fetchMasterEntities}
            renderHeader={renderHeader}
            renderRow={renderRow}
            searchPlaceholder={searchPlaceholder}
            createMasterEntity={createMasterEntity}
            pageSize={pageSize}
        />
    );
}
