import { useState, useCallback, useMemo, useEffect } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { artEntitiesKeys } from '../utils/query-keys.ts';
import { fetchPersons, type PersonPageSearchParams } from '../api/art-data-persons.ts';

/**
 * Hook for managing the persons table with pagination, search, and sorting.
 */
export function usePersonTable() {
    const queryClient = useQueryClient();

    const [params, setParams] = useState<PersonPageSearchParams>({
        page: 0,
        size: 20,
        search: '',
        sort: 'name,asc',
    });

    const queryKey = useMemo(() => artEntitiesKeys.persons(params), [params]);

    const pageQuery = useQuery({
        queryKey,
        queryFn: () => fetchPersons(params),
    });

    // Prefetch next page
    useEffect(() => {
        if (pageQuery.isSuccess && !pageQuery.isFetching && pageQuery.data) {
            if (params.page < pageQuery.data.totalPages - 1) {
                const nextParams = { ...params, page: params.page + 1 };
                const nextKey = artEntitiesKeys.persons(nextParams);
                if (!queryClient.getQueryData(nextKey)) {
                    const timeoutId = setTimeout(() => {
                        queryClient.prefetchQuery({
                            queryKey: nextKey,
                            queryFn: () => fetchPersons(nextParams),
                        });
                    }, 100);
                    return () => clearTimeout(timeoutId);
                }
            }
        }
    }, [pageQuery.isSuccess, pageQuery.isFetching, pageQuery.data, params, queryClient]);

    const setSearch = useCallback((search: string) =>
        setParams(prev => ({ ...prev, search, page: 0 })), []);

    const setSort = useCallback((sort: string) =>
        setParams(prev => ({ ...prev, sort })), []);

    const handleSearchSubmit = useCallback(() => {
        setParams(prev => ({ ...prev, page: 0 }));
    }, []);

    const nextPage = useCallback(() => {
        if (pageQuery.data && params.page < pageQuery.data.totalPages - 1) {
            setParams(prev => ({ ...prev, page: prev.page + 1 }));
        }
    }, [pageQuery.data?.totalPages, params.page]);

    const prevPage = useCallback(() => {
        if (params.page > 0) {
            setParams(prev => ({ ...prev, page: prev.page - 1 }));
        }
    }, [params.page]);

    const goToPage = useCallback((page: number) => {
        if (page >= 0 && pageQuery.data && page < pageQuery.data.totalPages) {
            setParams(prev => ({ ...prev, page }));
        }
    }, [pageQuery.data?.totalPages]);

    const refresh = useCallback(() => {
        queryClient.invalidateQueries({ queryKey: artEntitiesKeys.all });
    }, [queryClient]);

    const entities = useMemo(() => pageQuery.data?.content, [pageQuery.data?.content]);

    const pagination = useMemo(() => ({
        page: params.page,
        totalPages: pageQuery.data?.totalPages || 0,
        hasNextPage: pageQuery.data ? params.page < pageQuery.data.totalPages - 1 : false,
        hasPrevPage: params.page > 0,
    }), [params.page, pageQuery.data?.totalPages]);

    return {
        entities,
        pagination,
        search: params.search || '',
        sort: params.sort || 'name,asc',
        isLoading: pageQuery.isLoading,
        setSearch,
        setSort,
        nextPage,
        prevPage,
        goToPage,
        handleSearchSubmit,
        refresh,
    };
}
