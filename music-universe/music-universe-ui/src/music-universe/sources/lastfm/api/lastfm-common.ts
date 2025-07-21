import axios from 'axios';
import { LastfmConfig } from '@/music-universe/sources/lastfm/config/lastfmconfig';
import type { LastfmEntity } from '@/music-universe/sources/lastfm/types/lastfm-entity';
import type { MasterEntity } from '@/music-universe/shared/types/entities.ts';

/**
 * Generic function to update approval status for any LastFM entity
 * 
 * @param entity The LastFM entity to update
 * @param newStatus New approval status
 * @returns The same entity with updated approval status
 */
export async function updateApprovalStatus<T extends LastfmEntity<M>, M extends MasterEntity>(
    entity: T, 
    newStatus: number
): Promise<void> {

    // Map entity type to API endpoint
    const entityTypeToEndpoint: Record<string, string> = {
        'artist': 'artists',
        'track': 'tracks',
        'category': 'tags'
    };
    
    const entityType = entity.getEntityType();
    const endpoint = entityTypeToEndpoint[entityType];
    
    if (!endpoint) {
        throw new Error(`Unknown entity type: ${entityType}`);
    }
    
    try {
        // Make API call to update approval status
        await axios.patch(
            `${LastfmConfig.baseApiUrl}/${endpoint}/${entity.id}/approval`, 
            {
                approvalStatus: newStatus,
            }
        );
    } catch (error) {
        console.error(`Failed to update approval status for ${entityType} ${entity.id}:`, error);
        throw error;
    }
}
