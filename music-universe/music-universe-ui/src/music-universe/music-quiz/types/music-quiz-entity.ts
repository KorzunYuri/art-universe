import type { MasterEntityType } from "@/music-universe/shared/types/entities.ts";

/**
 * Supported entity types for Music Quiz module
 * Currently supports artists and tracks for quiz generation
 */
export type MusicQuizSupportedEntityType = Extract<
    MasterEntityType,
    'artist' | 'track'
>;
