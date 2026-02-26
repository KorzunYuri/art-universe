import type {
    BaseLookupParams,
    BaseLookupRequest,
    LookupConfiguration,
    LookupEntity
} from "@/shared/types/lookup";
import type { MasterEntityType } from "@/music/shared/types/entities";
import type { DataSource } from "@/music/data/raw/shared/types/data-sources";

/**
 * Central registry for lookup configurations
 * Maps (dataSource, entityType) pairs to their lookup configurations
 * 
 * This registry stores both parameter transformation and lookup functions
 */
class LookupRegistryClass {
    private configurations = new Map<string, LookupConfiguration<any, any>>();

    /**
     * Creates a unique key for the registry
     */
    private createKey(dataSource: DataSource | 'master', entityType: MasterEntityType): string {
        return `${dataSource}:${entityType}`;
    }

    /**
     * Register a lookup configuration with both transformer and lookup function
     * This method is called by specific modules to register their lookup capabilities
     */
    register<TParams extends BaseLookupParams, TRequest extends BaseLookupRequest>(
        dataSource: DataSource | 'master',
        entityType: MasterEntityType,
        config: LookupConfiguration<TParams, TRequest>
    ): void {
        const key = this.createKey(dataSource, entityType);
        this.configurations.set(key, config);
        console.log(`🔧 Registered lookup configuration: ${key}`);
    }

    /**
     * Execute lookup with parameter transformation
     */
    async lookup<TParams extends BaseLookupParams>(
        dataSource: DataSource | 'master',
        entityType: MasterEntityType,
        params: TParams
    ): Promise<LookupEntity[]> {
        const key = this.createKey(dataSource, entityType);
        const config = this.configurations.get(key);
        
        if (!config) {
            console.warn(`No lookup configuration found for ${key}`);
            return [];
        }

        // Transform parameters to request
        const request = config.transformParams(params);
        
        // Execute lookup
        return await config.lookupEntities(request);
    }

    /**
     * Checks if a configuration exists
     */
    has(dataSource: DataSource | 'master', entityType: MasterEntityType): boolean {
        const key = this.createKey(dataSource, entityType);
        return this.configurations.has(key);
    }

    /**
     * Lists all registered configurations (for debugging)
     */
    listRegistered(): string[] {
        return Array.from(this.configurations.keys());
    }
}

// Create singleton instance
export const LookupRegistry = new LookupRegistryClass();
