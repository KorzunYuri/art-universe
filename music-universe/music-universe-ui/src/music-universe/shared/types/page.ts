export interface SortMeta {
    empty: boolean
    sorted: boolean
    unsorted: boolean
}

export interface PageableMeta {
    pageNumber: number
    pageSize: number
    sort: SortMeta
    offset: number
    paged: boolean
    unpaged: boolean
}

export interface Page<T> {
    content: T[]
    pageable: PageableMeta
    last: boolean
    totalPages: number
    totalElements: number
    size: number
    number: number
    sort: SortMeta
    first: boolean
    numberOfElements: number
    empty: boolean
}

export interface BasePageSearchParams {
    page: number;
    search?: string;
    size?: number;
    sort?: string;
}