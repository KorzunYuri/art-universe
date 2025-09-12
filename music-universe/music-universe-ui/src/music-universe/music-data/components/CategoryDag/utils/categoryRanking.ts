export type MetricType = 'childrenCount' | 'artistsCount' | 'tracksCount';

export interface CategoryMetrics {
    childrenCount: number;
    artistsCount: number;
    tracksCount: number;
}

export interface RankedCategory {
    id: number;
    name: string;
    isRoot: boolean;
    metrics: CategoryMetrics;
    rank: number; // 1-5, where 5 is highest
    value: number; // actual metric value used for ranking
}

/**
 * Ranks categories into 5 groups based on selected metric
 */
export function rankCategories(
    categories: Array<{ id: number; name: string; isRoot: boolean; metrics: CategoryMetrics }>,
    metricType: MetricType
): RankedCategory[] {
    // Extract values for the selected metric
    const values = categories.map(cat => cat.metrics[metricType]);
    const sortedValues = [...values].sort((a, b) => a - b);
    
    // Calculate quintile thresholds
    const getThreshold = (percentile: number) => {
        const index = Math.floor((sortedValues.length - 1) * percentile);
        return sortedValues[index];
    };
    
    const thresholds = [
        getThreshold(0.2),  // 20th percentile
        getThreshold(0.4),  // 40th percentile  
        getThreshold(0.6),  // 60th percentile
        getThreshold(0.8),  // 80th percentile
    ];
    
    return categories.map(category => {
        const value = category.metrics[metricType];
        let rank = 1;
        
        for (let i = 0; i < thresholds.length; i++) {
            if (value > thresholds[i]) {
                rank = i + 2;
            }
        }
        
        return {
            ...category,
            rank,
            value
        };
    });
}
