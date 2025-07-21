import type { RawEntity, MasterEntity } from '@/music-universe/shared/types/entities.ts';
import type { Approvable } from '@/music-universe/shared/types/approvable';

/**
 * Interface for LastFM entities that are both raw entities and approvable
 * @template M The type of master entity this LastFM entity can be bound to
 */
export interface LastfmEntity<M extends MasterEntity = MasterEntity> extends RawEntity<M>, Approvable {
    approvalStatus: number;
    
    /**
     * Sets the approval status for this LastFM entity
     * @param approvalStatus The new approval status
     */
    setApprovalStatus(approvalStatus: number): void;
}
