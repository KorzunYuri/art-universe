import axios from 'axios';
import { LastfmConfig } from '@/music-universe/sources/lastfm/config/lastfmconfig';

/**
 * Triggers database maintenance for LastFM module
 * 
 * @returns Promise that resolves with maintenance response message
 */
export async function triggerDbMaintenance(): Promise<string> {
    console.log('🔧 Triggering LastFM database maintenance...');
    
    const response = await axios.get<string>(
        `${LastfmConfig.baseApiUrl}/maintenance/trigger`
    );
    
    return response.data;
}
