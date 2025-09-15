import type {MasterEntityType} from "@/music-universe/shared/types/entities.ts";
import type {DataSource} from "@/music-universe/sources/shared/types/data-sources.ts";
import type { BaseLookupParams } from "@/music-universe/shared/types/lookup.ts";
/**
 * keys for raw entities
 */
export const rawEntitiesKeys = {
  all: ['rawEntities'] as const,
  source: (dataSource: DataSource) =>
    [...rawEntitiesKeys.all, dataSource] as const,
  type:   (dataSource: DataSource, entityType: MasterEntityType) =>
    [...rawEntitiesKeys.source(dataSource), entityType] as const,
  list:   (dataSource: DataSource, entityType: MasterEntityType, params?: Record<string, any>) =>
    [...rawEntitiesKeys.type(dataSource, entityType), params] as const,
  detail: (dataSource: DataSource, entityType: MasterEntityType, id: number) =>
    [...rawEntitiesKeys.type(dataSource, entityType), 'detail', id] as const,
};

/**
 * keys for master entities
 */
export const masterEntitiesKeys = {
  all: ['masterEntities'] as const,
  type:   (entityType: MasterEntityType) =>
    [...masterEntitiesKeys.all, entityType] as const,
  list:   (entityType: MasterEntityType, params?: Record<string, any>) =>
    [...masterEntitiesKeys.type(entityType), params] as const,
  detail: (entityType: MasterEntityType, id: number) =>
    [...masterEntitiesKeys.type(entityType), 'detail', id] as const,
  withRelations: (entityType: MasterEntityType, params?: Record<string, any>) =>
    [...masterEntitiesKeys.type(entityType), 'with-relations', params] as const,
  withRelationsDetail: (entityType: MasterEntityType, id: number) =>
    [...masterEntitiesKeys.type(entityType), 'with-relations', 'detail', id] as const,
};

/**
 * keys for lookup
 */
export const entityLookupKeys = {
  all: ['lookup'] as const,
  dataSource: (dataSource: DataSource | 'master') =>
      [...entityLookupKeys.all, dataSource] as const,
  type:   (dataSource: DataSource | 'master', entityType: MasterEntityType) =>
    [...entityLookupKeys.dataSource(dataSource), entityType] as const,
  query:  (dataSource: DataSource | 'master', entityType: MasterEntityType, params: BaseLookupParams) =>
    [...entityLookupKeys.type(dataSource, entityType), params] as const,
};
