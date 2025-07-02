// hooks
import { useEffect, useState } from 'react'
// types
import type { Page } from '@/music-universe/shared/types/page'
import type { Dimension, DimensionSearchParams, DimensionSaveRequest } from '@/music-universe/music-data/api/music-data-dimensions'
// api
import { fetchDimensions, saveDimension } from '@/music-universe/music-data/api/music-data-dimensions'
// components
import { DimensionsTableHeader } from '../DimensionsTableHeader'
import { DimensionsTableRow } from '../DimensionsTableRow'
import { EditableText } from '@/music-universe/shared/components'
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss'
import styles from './DimensionsTable.module.css'

export const DimensionsTable = () => {
    // Search state
    const [searchName, setSearchName] = useState('')
    
    // Data state
    const [data, setData] = useState<Page<Dimension> | null>(null)
    const [loading, setLoading] = useState(false)
    const [page, setPage] = useState(0)
    const [sort, setSort] = useState('name,asc')
    const pageSize = 20

    // New dimension state
    const [newDimensionName, setNewDimensionName] = useState('')
    const [isCreating, setIsCreating] = useState(false)

    // Load dimensions with current search parameters
    const loadDimensions = async () => {
        setLoading(true)
        try {
            const params: DimensionSearchParams = {
                search: searchName || undefined,
                page,
                size: pageSize,
                sort
            }
            
            const result = await fetchDimensions(params)
            setData(result)
        } catch (error) {
            console.error('❌ Error loading dimensions:', error)
        } finally {
            setLoading(false)
        }
    }

    // Load dimensions when search parameters or pagination changes
    useEffect(() => {
        loadDimensions()
    }, [page, sort])

    // Handle dimension changes from child components
    const onDimensionChanged = (updated: Dimension) => {
        if (!data) return
        
        console.log(`🔄 Dimension changed: (${updated.name})`)
        
        const newContent = data.content.map(d => d.id === updated.id ? updated : d)
        setData({ ...data, content: newContent })
    }

    // Handle search form submission
    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault()
        console.log('🔍 Search submitted')
        setPage(0) // Reset to first page when searching
        loadDimensions()
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
        loadDimensions()
    }

    // Handle creating new dimension
    const handleCreateDimension = async () => {
        if (!newDimensionName.trim()) return

        setIsCreating(true)
        try {
            const saveRequest: DimensionSaveRequest = {
                name: newDimensionName.trim()
            }

            const savedDimension = await saveDimension(saveRequest)
            if (savedDimension) {
                console.log('✅ New dimension created successfully')
                setNewDimensionName('') // Clear input
                loadDimensions() // Refresh table
            } else {
                console.error('❌ Failed to create dimension')
            }
        } catch (error) {
            console.error('❌ Error creating dimension:', error)
        } finally {
            setIsCreating(false)
        }
    }

    return (
        <div className={styles.container}>
            <form onSubmit={handleSearch} className={styles.searchForm}>
                <div className={styles.searchRow}>
                    <div className={`${styles.searchField} ${styles.nameField}`}>
                        <input
                            type="text"
                            value={searchName}
                            placeholder="Search dimension name..."
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

            {/* New dimension creation row */}
            <div className={styles.createRow}>
                <div className={styles.createField}>
                    <EditableText
                        value={newDimensionName}
                        onChange={setNewDimensionName}
                        placeholder="New dimension name..."
                        disabled={isCreating}
                    />
                </div>
                <div className={styles.createActions}>
                    <button
                        onClick={handleCreateDimension}
                        disabled={isCreating || !newDimensionName.trim()}
                        className={styles.addButton}
                    >
                        {isCreating ? "..." : "Add"}
                    </button>
                </div>
            </div>

            {!loading && data && (
                <>
                    <div className={styles.tableContainer}>
                        <DimensionsTableHeader
                            sort={sort}
                            setSort={setSort}
                        />
                        {data.content.map((dimension) => (
                            <DimensionsTableRow
                                key={dimension.id}
                                dimension={dimension}
                                onChange={onDimensionChanged}
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
