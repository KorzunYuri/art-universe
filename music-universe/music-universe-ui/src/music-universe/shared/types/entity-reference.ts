/**
 * Base interface for all entities
 */
export interface BaseEntity {
    id: number;
    name: string;
}

/**
 * Interface for entities that can hold a reference to a master entity
 */
export interface MasterEntityHolder {
    /**
     * Returns the master entity if available
     */
    getMasterEntity(): MasterEntity | undefined;
    
    /**
     * Checks if the entity has a master entity reference
     */
    hasMasterEntity(): boolean;
}

/**
 * Interface for master entities (from music-data)
 * Master entities always reference themselves
 */
export interface MasterEntity extends BaseEntity, MasterEntityHolder {
    /**
     * Returns the master entity (self-reference for MasterEntity)
     */
    getMasterEntity(): MasterEntity;
    
    /**
     * Always returns true for MasterEntity
     */
    hasMasterEntity(): true;
    
    /**
     * Returns the entity type
     */
    getEntityType(): string;
}

/**
 * Interface for raw entities (from external sources like LastFM)
 * Can be bound to a master entity
 * @template M The type of master entity this raw entity can be bound to
 */
export interface RawEntity<M extends MasterEntity = MasterEntity> extends BaseEntity, MasterEntityHolder {
    /**
     * Reference to the master entity this raw entity is bound to
     */
    masterEntity?: M;
    
    /**
     * Returns the master entity if available
     */
    getMasterEntity(): M | undefined;
    
    /**
     * Checks if the entity has a master entity reference
     */
    hasMasterEntity(): boolean;
    
    /**
     * Sets the master entity for this raw entity
     * @param masterEntity The master entity to bind to this raw entity
     */
    setMasterEntity(masterEntity: M | undefined): void;
    
    /**
     * Returns the entity type
     */
    getEntityType(): string;
}

/**
 * Default implementation of MasterEntity
 */
export class DefaultMasterEntity implements MasterEntity {
    id: number;
    name: string;
    
    constructor(id: number, name: string) {
        this.id = id;
        this.name = name;
    }
    
    getMasterEntity(): MasterEntity {
        return this;
    }
    
    hasMasterEntity(): true {
        return true;
    }
    
    getEntityType(): string {
        return 'UNKNOWN';
    }
}

/**
 * Base class for raw entities with common method implementations
 */
export abstract class BaseRawEntity<M extends MasterEntity = MasterEntity> implements RawEntity<M> {
    id: number;
    name: string;
    masterEntity?: M;
    
    constructor(id: number, name: string, masterEntity?: M) {
        this.id = id;
        this.name = name;
        this.masterEntity = masterEntity;
    }
    
    getMasterEntity(): M | undefined {
        return this.masterEntity;
    }
    
    hasMasterEntity(): boolean {
        return this.masterEntity !== undefined;
    }
    
    setMasterEntity(masterEntity: M | undefined): void {
        this.masterEntity = masterEntity;
    }
    
    abstract getEntityType(): string;
}