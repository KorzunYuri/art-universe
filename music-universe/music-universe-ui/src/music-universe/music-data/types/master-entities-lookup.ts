/**
 * Common entity structure returned by lookup operations
 */
export interface LookupEntity {
    id: number;
    name: string;
}

/**
 * Generic request DTO for batch lookup operations
 */
export interface BatchLookupRequestDTO {
    searchTerms: string[];
    limit?: number;
}

/**
 * Generic response DTO for batch lookup operations
 */
export interface BatchLookupResponseDTO {
    results: {
        [name: string]: LookupEntity[];
    };
}
