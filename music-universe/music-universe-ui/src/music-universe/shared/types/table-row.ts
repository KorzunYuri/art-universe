import type {BaseEntity} from "@/music-universe/shared/types/entities.ts";

/**
 * Base interface for raw entity table row components
 * Ensures type safety when working with raw entities from external sources
 */
export interface BaseEntityTableRow {
    entityId: number
}

export interface LegacyEntityTableRow<T extends BaseEntity> {
    entity: T;
}