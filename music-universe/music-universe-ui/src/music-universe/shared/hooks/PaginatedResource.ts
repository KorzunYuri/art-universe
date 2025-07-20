import { useEffect, useState, useCallback } from 'react'
import type { Page } from '@/music-universe/shared/types/page'

type FetchPage<T> = (params: {
    page: number
    size: number
    search?: string
    sort?: string
}) => Promise<Page<T>>

interface PaginatedResourceOptions {
    initialSearch?: string
    initialSort?: string
    pageSize?: number
}

export function PaginatedResource<T>(
    fetchPage: FetchPage<T>,
    options?: PaginatedResourceOptions
) {
    const [data, setData] = useState<Page<T> | null>(null)
    const [loading, setLoading] = useState(false)
    const [searchInput, setSearchInput] = useState(options?.initialSearch || '')
    const [search, setSearch] = useState(options?.initialSearch || '')
    const [sort, setSort] = useState(options?.initialSort || '')
    const [page, setPage] = useState(0)
    const size = options?.pageSize || 10

    const load = useCallback(async () => {
        setLoading(true)
        try {
            const result = await fetchPage({ page, size, search, sort })
            setData(result)
        } finally {
            setLoading(false)
        }
    }, [page, size, search, sort])

    useEffect(() => {
        load()
    }, [load])

    const applySearch = () => {
        if (searchInput !== search) {
            setSearch(searchInput)
            setPage(0)
        }
    }

    const nextPage = () => {
        if (data && data.pageable.pageNumber + 1 < data.totalPages) {
            setPage(data.pageable.pageNumber + 1)
        }
    }

    const prevPage = () => {
        if (data && data.pageable.pageNumber > 0) {
            setPage(data.pageable.pageNumber - 1)
        }
    }

    return {
        data,
        loading,
        searchInput,
        setSearchInput,
        applySearch,
        sort,
        setSort,
        nextPage,
        prevPage,
        hasNextPage: data ? data.pageable.pageNumber + 1 < data.totalPages : false,
        hasPrevPage: data ? data.pageable.pageNumber > 0 : false,
        reload: load,
    }
}