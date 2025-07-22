import axios from 'axios';
import { LastfmConfig } from '@/music-universe/sources/lastfm/config/lastfmconfig';
import type {MasterEntityType} from "@/music-universe/music-data/types/master-entities.ts";
import type {ApprovalStatusType} from "@/music-universe/sources/lastfm/constants/approvalStatus.ts";

// Map entity type to API endpoint
const entityTypeToEndpoint: Record<MasterEntityType, string> = {
    'artist': 'artists',
    'album': 'albums',
    'track': 'tracks',
    'category': 'tags',
    'dimension': '' // Not applicable for LastFM
};

/**
 * Generic function to update approval status for any LastFM entity
 *
 * @param entityType
 * @param entityId
 * @param newStatus New approval status
 * @returns The same entity with updated approval status
 */
export async function updateApprovalStatus(
    entityType: MasterEntityType,
    entityId: number,
    newStatus: ApprovalStatusType
): Promise<void> {
    
    const endpoint = entityTypeToEndpoint[entityType];
    
    if (!endpoint) {
        throw new Error(`Unknown entity type: ${entityType}`);
    }
    
    try {
        // Make API call to update approval status
        await axios.patch(
            `${LastfmConfig.baseApiUrl}/${endpoint}/${entityId}/approval`,
            {
                approvalStatus: newStatus,
            }
        );
    } catch (error) {
        console.error(`Failed to update approval status for ${entityType} ${entityId}:`, error);
        throw error;
    }
}
