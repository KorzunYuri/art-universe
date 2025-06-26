/**
 * Standard API response structure used across all backend modules
 */
export interface ApiResponse<T> {
    success: boolean;
    message?: string;
    data: T;
}
