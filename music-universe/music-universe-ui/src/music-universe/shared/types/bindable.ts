/**
 * Interface for bound entity reference
 */
export interface BoundEntity {
    referenceId: number;
    referenceName: string;
}

/**
 * Interface for bound entity from API response
 */
export interface BoundEntityResponse {
    externalId: number;
    dataSource: string;
    referenceId: number;
    referenceName: string;
}

/**
 * Interface for entities that can be bound to internal entities
 */
export interface Bindable {
    id: number;
    name: string; // source entity name
    boundEntity?: BoundEntity;
}
