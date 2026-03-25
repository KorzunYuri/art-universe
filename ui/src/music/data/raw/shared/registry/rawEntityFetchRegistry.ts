import type { RawEntity, MasterEntityType } from "@/music/shared/types/entities.ts";
import type { DataSource } from "@/music/data/raw/shared/types/data-sources.ts";

type RawEntityFetcher = <T extends MasterEntityType>(entityType: T, id: number) => Promise<RawEntity<T>>;

const registry: Record<string, RawEntityFetcher> = {};

export function registerRawEntityFetcher(dataSource: DataSource, fetchFn: RawEntityFetcher) {
    registry[dataSource] = fetchFn;
}

export function getRawEntityFetcher(dataSource: DataSource): RawEntityFetcher {
    const fetcher = registry[dataSource];
    if (!fetcher) {
        throw new Error(`No raw entity fetcher registered for data source: ${dataSource}`);
    }
    return fetcher;
}
