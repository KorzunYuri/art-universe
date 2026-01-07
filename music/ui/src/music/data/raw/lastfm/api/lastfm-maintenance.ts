import { LastfmConfig } from '@/music/data/raw/lastfm/config/lastfmconfig.ts';

const lastfmWriteApi = LastfmConfig.writeApi;

/**
 * Triggers database maintenance for LastFM module
 *
 * @returns Promise that resolves with maintenance response message
 */
export async function triggerDbMaintenance(): Promise<string> {
    console.log('🔧 Triggering LastFM database maintenance...');

    const response = await lastfmWriteApi.get<string>(
        `/maintenance/trigger`
    );

    return response.data;
}
