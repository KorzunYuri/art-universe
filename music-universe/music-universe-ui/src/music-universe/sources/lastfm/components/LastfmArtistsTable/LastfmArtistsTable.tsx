// hooks
import { useEffect, useState } from 'react'
// components
import {
    LastfmArtistsTableHeader,
    LastfmArtistsTableRow,
} from '@/music-universe/sources/lastfm/components'
// types
import type { LastfmArtist } from '@/music-universe/sources/lastfm/types'
import type { Page } from '@/music-universe/shared/types/page'
// api
import { fetchArtists, type ArtistSearchParams } from '@/music-universe/sources/lastfm/api/lastfm-artists'
import { fetchBoundArtists } from '@/music-universe/music-data/api/music-data-artists'
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss'
import styles from './LastfmArtistsTable.module.css'

export const LastfmArtistsTable = () => {
    // Search state
    const [searchName, setSearchName] = useState('')
    const [minPlayCount, setMinPlayCount] = useState<number | undefined>(undefined)
    const [minListenersCount, setMinListenersCount] = useState<number | undefined>(undefined)
    const [approvalStatuses, setApprovalStatuses] = useState<number[] | undefined>(undefined)
    
    // Data state
    const [data, setData] = useState<Page<LastfmArtist> | null>(null)
    const [loading, setLoading] = useState(false)
    const [loadingBoundArtists, setLoadingBoundArtists] = useState(false)
    const [page, setPage] = useState(0)
    const [sort, setSort] = useState('name,asc')
    const pageSize = 20

    // Load artists with current search parameters and their bound entities
    const loadArtists = async () => {
        setLoading(true)
        try {
            const params: ArtistSearchParams = {
                search: searchName || undefined,
                minPlayCount: minPlayCount,
                minListenersCount: minListenersCount,
                approvalStatuses: approvalStatuses,
                page,
                size: pageSize,
                sort
            }
            
            console.log('🔄 Loading artists...')
            const result = await fetchArtists(params)
            
            // Load bound artists immediately in the same function to avoid useEffect cycles
            if (result && result.content.length > 0) {
                console.log('🔄 Loading bound artists immediately after artists load')
                setLoadingBoundArtists(true)
                try {
                    // Extract all artist IDs
                    const artistIds = result.content.map(artist => artist.id)
                    console.log(`📋 Requesting bindings for ${artistIds.length} artists: ${artistIds.join(', ')}`)
                    
                    // Fetch bound artists from music-data API
                    const boundArtists = await fetchBoundArtists(artistIds)
                    console.log(`✅ Bound artists loaded: ${boundArtists.length} items`)

                    // Update the artists with bound information
                    const updatedContent = result.content.map(artist => {
                        const boundArtist = boundArtists.find(ba => ba.externalId === artist.id)
                        if (boundArtist) {
                            return {
                                ...artist,
                                boundEntity: {
                                    referenceId: boundArtist.referenceId,
                                    referenceName: boundArtist.referenceName
                                }
                            }
                        }
                        // explicitly set boundEntity for non-bound artists
                        return {
                            ...artist,
                            boundEntity: undefined
                        }
                    })
                    
                    console.log('📝 Setting final data with bound artists')
                    setData({ ...result, content: updatedContent })
                } catch (error) {
                    console.error('❌ Error loading bound artists:', error)
                    // Set data without bound artists if binding fails
                    setData(result)
                } finally {
                    setLoadingBoundArtists(false)
                }
            } else {
                // Set data even if no content
                setData(result)
            }
        } catch (error) {
            console.error('❌ Error loading artists:', error)
        } finally {
            setLoading(false)
        }
    }

    // Load artists when search parameters or pagination changes
    useEffect(() => {
        console.log('🔄 Effect triggered: page or sort changed')
        loadArtists()
    }, [page, sort])

    // Handle artist changes from child components
    const onArtistChanged = (updated: LastfmArtist) => {
        if (!data) return
        
        console.log(`🔄 Artist changed: (${updated.name})`)
        
        const newContent = data.content.map(a => a.id === updated.id ? updated : a)
        setData({ ...data, content: newContent })
    }

    // Handle search form submission
    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault()
        console.log('🔍 Search submitted')
        setPage(0) // Reset to first page when searching
        loadArtists()
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
        loadArtists()
    }

    // Handle number input changes with validation
    const handleNumberChange = (
        value: string, 
        setter: React.Dispatch<React.SetStateAction<number | undefined>>
    ) => {
        if (value === '') {
            setter(undefined)
        } else {
            const num = parseInt(value, 10)
            if (!isNaN(num) && num >= 0) {
                setter(num)
            }
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
                            placeholder="Search artist name..."
                            onChange={(e) => setSearchName(e.target.value)}
                            className={commonStyles.muLabel}
                        />
                    </div>
                    
                    <div className={`${styles.searchField} ${styles.mbidField}`}>
                        {/* MBID field is not searchable */}
                    </div>
                    
                    <div className={`${styles.searchField} ${styles.statusField}`}>
                        <select 
                            value={approvalStatuses?.join(',') || ''}
                            onChange={(e) => {
                                const value = e.target.value
                                setApprovalStatuses(value ? value.split(',').map(Number) : undefined)
                            }}
                            className={commonStyles.muLabel}
                        >
                            <option value="">All statuses</option>
                            <option value="1">Pending</option>
                            <option value="2">Approved</option>
                            <option value="3">Declined</option>
                            <option value="4">Auto-approved</option>
                            <option value="2,4">Approved & Auto-approved</option>
                        </select>
                    </div>
                    
                    <div className={`${styles.searchField} ${styles.bindingField}`}>
                        {/* Binding field is not searchable */}
                    </div>
                    
                    <div className={`${styles.searchField} ${styles.countField}`}>
                        <input
                            type="text"
                            value={minPlayCount === undefined ? '' : minPlayCount}
                            placeholder="Min plays"
                            onChange={(e) => handleNumberChange(e.target.value, setMinPlayCount)}
                            className={commonStyles.muLabel}
                        />
                    </div>
                    
                    <div className={`${styles.searchField} ${styles.countField}`}>
                        <input
                            type="text"
                            value={minListenersCount === undefined ? '' : minListenersCount}
                            placeholder="Min listeners"
                            onChange={(e) => handleNumberChange(e.target.value, setMinListenersCount)}
                            className={commonStyles.muLabel}
                        />
                    </div>
                </div>
                
                <div className={styles.searchActions}>
                    <button type="submit" disabled={loading || loadingBoundArtists}>
                        Search
                    </button>
                    <button type="button" onClick={handleRefresh} disabled={loading || loadingBoundArtists}>
                        Refresh
                    </button>
                    
                    {(loading || loadingBoundArtists) && (
                        <div className={styles.loading}>Loading...</div>
                    )}
                </div>
            </form>

            {!loading && data && (
                <>
                    <div className={styles.tableContainer}>
                        <LastfmArtistsTableHeader
                            sort={sort}
                            setSort={setSort}
                        />
                        {data.content.map((artist) => (
                            <LastfmArtistsTableRow
                                key={artist.id}
                                artist={artist}
                                onChange={onArtistChanged}
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
