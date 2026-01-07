/**
 * Basic lookup context for simple entity searches
 */
export interface BasicLookupContext {
    type: 'basic';
}

/**
 * Factory for creating basic lookup contexts
 */
export const LookupContextFactory = {
    /**
     * Creates basic lookup context for simple searches
     */
    basic(): BasicLookupContext {
        return { type: 'basic' };
    }
};
