import type {RawEntity, MasterEntity, BaseEntity} from './entities.ts';

/**
 * Generic interface for entity table row components
 * Works with any entity that extends BaseEntity (both raw and master entities)
 */
export interface EntityTableRow<T extends BaseEntity> {
    /**
     * The entity to display in this row
     **/
    entity: T;
}

/**
 * Base interface for raw entity table row components
 * Ensures type safety when working with raw entities from external sources
 */
export interface RawEntityTableRow<T extends RawEntity> extends EntityTableRow<T>{
}

/**
 * Base interface for master entity table row components
 * Ensures type safety when working with master entities from music-data
 */
export interface MasterEntityTableRow<T extends MasterEntity> extends EntityTableRow<T> {
}