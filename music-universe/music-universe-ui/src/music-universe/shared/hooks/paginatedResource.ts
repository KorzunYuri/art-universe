import { useEffect, useState } from 'react'
import type { Page } from '@/music-universe/shared/types/page'
import * as sea from "node:sea";

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
    const [meta, setMeta] = useState<Page<T> | null>(null)
    const [loading, setLoading] = useState(false)
    const [search, setSearch] = useState(options?.initialSearch || '')
    const [sort, setSort] = useState(options?.initialSort || '')
    const [page, setPage] = useState(0)
    const size = options?.pageSize || 10

    const load = async () => {
        setLoading(true)
        try {
            const result = await fetchPage({ page, size, search, sort })
            setMeta(result)
        } finally {
            setLoading(false)
        }
    }

    const setNewSearch = (newSearch: string) => {
        if (newSearch != search) {
            setPage(0)
        }
        setSearch(newSearch)
    }

    useEffect(() => {
        load()
    }, [page, search, sort])

    return {
        meta,
        loading,
        search,
        setNewSearch,
        sort,
        setSort,
        page,
        setPage,
        size,
        reload: load,
    }
}