import { DefaultMasterEntity, type MasterEntity } from "../types/entity-reference";

/**
 * Creates a MasterEntity object with the required methods
 * 
 * @param id Entity ID
 * @param name Entity name
 * @returns MasterEntity object with getMasterEntity and hasMasterEntity methods
 */
export function createMasterEntity(id: number, name: string): MasterEntity {
    return new DefaultMasterEntity(id, name);
}
