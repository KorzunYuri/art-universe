// hooks
import { useEffect, useState } from 'react'
// types
import type { Page } from '@/music-universe/shared/types/page'
import type { Category, CategorySearchParams } from '@/music-universe/music-data/api/music-data-categories'
import type { LookupEntity } from '@/music-universe/shared/types/lookup'
// api
import { fetchCategories, batchLookupCategories } from '@/music-universe/music-data/api/music-data-categories'
// components
import { CategoriesTableHeader } from '../CategoriesTableHeader'
import { CategoriesTableRow } from '../CategoriesTableRow'
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss'
import styles from './CategoriesTable.module.css'

export const CategoriesTable = () => {
    // Search state
    const [searchName, setSearchName] = useState('')
    
    // Data state
    const [data, setData] = useState<Page<Category> | null>(null)
    const [loading, setLoading] = useState(false)
    const [page, setPage] = useState(0)
    const [sort, setSort] = useState('name,asc')
    const pageSize = 20
    
    // Preloaded lookup data for categories and dimensions
    const [preloadedCategoryData, setPreloadedCategoryData] = useState<{[name: string]: LookupEntity[]}>({})
    // @ts-expect-error @typescript-eslint/no-unused-vars
    const [loadingPreloadedData, setLoadingPreloadedData] = useState(false)

    // Load categories with current search parameters
    const loadCategories = async () => {
        setLoading(true)
        try {
            const params: CategorySearchParams = {
                search: searchName || undefined,
                page,
                size: pageSize,
                sort
            }
            
            const result = await fetchCategories(params)
            setData(result)
            
            // After loading categories, perform batch lookup for category names
            if (result && result.content.length > 0) {
                setLoadingPreloadedData(true)
                try {
                    // Collect category names for batch lookup
                    const categoryNames = result.content.map(category => category.name)
                    
                    // Perform batch lookup for all categories
                    console.log(`🔍 Performing batch lookup for ${categoryNames.length} categories`)
                    const lookupResponse = await batchLookupCategories(categoryNames)
                    
                    if (lookupResponse.success) {
                        console.log(`✅ Batch lookup successful with ${Object.keys(lookupResponse.data.results).length} results`)
                        setPreloadedCategoryData(lookupResponse.data.results)
                    } else {
                        console.error('❌ Batch lookup failed:', lookupResponse.message)
                        setPreloadedCategoryData({})
                    }
                } catch (error) {
                    console.error('❌ Error performing batch lookup:', error)
                    setPreloadedCategoryData({})
                } finally {
                    setLoadingPreloadedData(false)
                }
            }
        } catch (error) {
            console.error('❌ Error loading categories:', error)
        } finally {
            setLoading(false)
        }
    }

    // Load categories when search parameters or pagination changes
    useEffect(() => {
        console.log('🔄 Effect triggered: page or sort changed')
        loadCategories()
    }, [page, sort])

    // Handle category changes from child components
    const onCategoryChanged = (updated: Category) => {
        if (!data) return
        
        console.log(`🔄 Category changed: (${updated.name})`)
        
        const newContent = data.content.map(c => c.id === updated.id ? updated : c)
        setData({ ...data, content: newContent })
    }

    // Handle search form submission
    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault()
        console.log('🔍 Search submitted')
        setPage(0) // Reset to first page when searching
        loadCategories()
    }

    // Handle pagination
    const nextPage = () => {
        if (data && page + 1 < data.totalPages) {
            console.log('📄 Moving to next page')
            setPage(page + 1)
        }
    }

    const prevPage = () => {
        if (page > 0) {
            console.log('📄 Moving to previous page')
            setPage(page - 1)
        }
    }

    // Handle refresh button
    const handleRefresh = () => {
        console.log('🔄 Refresh button clicked')
        loadCategories()
    }

    return (
        <div className={styles.container}>
            <form onSubmit={handleSearch} className={styles.searchForm}>
                <div className={styles.searchRow}>
                    <div className={`${styles.searchField} ${styles.nameField}`}>
                        <input
                            type="text"
                            value={searchName}
                            placeholder="Search category name..."
                            onChange={(e) => setSearchName(e.target.value)}
                            className={commonStyles.muLabel}
                        />
                    </div>
                </div>
                
                <div className={styles.searchActions}>
                    <button type="submit" disabled={loading}>
                        Search
                    </button>
                    <button type="button" onClick={handleRefresh} disabled={loading}>
                        Refresh
                    </button>
                    
                    {loading && (
                        <div className={styles.loading}>Loading...</div>
                    )}
                </div>
            </form>

            {!loading && data && (
                <>
                    <div className={styles.tableContainer}>
                        <CategoriesTableHeader
                            sort={sort}
                            setSort={setSort}
                        />
                        {data.content.map((category) => (
                            <CategoriesTableRow
                                key={category.id}
                                category={category}
                                onChange={onCategoryChanged}
                                preloadedLookupData={preloadedCategoryData[category.name] || []}
                            />
                        ))}
                    </div>

                    <div className={styles.pagination}>
                        <button disabled={page === 0} onClick={prevPage}>
                            Previous
                        </button>
                        <span>
                            Page {page + 1} of {data.totalPages}
                        </span>
                        <button disabled={page + 1 >= data.totalPages} onClick={nextPage}>
                            Next
                        </button>
                    </div>
                </>
            )}
        </div>
    )
}
