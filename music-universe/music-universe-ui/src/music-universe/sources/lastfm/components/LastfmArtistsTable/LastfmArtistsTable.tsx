// hooks
import { useEffect, useState, useRef } from 'react'
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
import { fetchBoundArtists } from '@/music-universe/sources/music-data/api/music-data-artists'
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
    
    // Refs for optimization
    const skipNextFetchRef = useRef(false)
    const previousContentRef = useRef<LastfmArtist[]>([])

    // Load artists with current search parameters
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
            
            const result = await fetchArtists(params)
            setData(result)
            
            // load bindings straight away
            // save result in a local variable to not trigger rerender
            const tempData = result;
            
            if (tempData && tempData.content.length > 0) {
                console.log('🔄 Directly loading bound artists after artists load')
                setLoadingBoundArtists(true)
                try {
                    // Extract all artist IDs
                    const artistIds = tempData.content.map(artist => artist.id)
                    console.log(`📋 Requesting bindings for ${artistIds.length} artists: ${artistIds.join(', ')}`)
                    
                    // Fetch bound artists from music-data API
                    const boundArtists = await fetchBoundArtists(artistIds)
                    console.log(`✅ Bound artists loaded: ${boundArtists.length} items`)
                    console.log('📊 Bound artists data:', boundArtists)
                    
                    // Update the artists with bound information
                    const updatedContent = tempData.content.map(artist => {
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
                    
                    console.log('📝 Updating artists with binding information')
                    // set a flag to skip loadBoundArtists in the upcoming rerender
                    skipNextFetchRef.current = true
                    previousContentRef.current = [...updatedContent]
                    setData({ ...tempData, content: updatedContent })
                } catch (error) {
                    console.error('❌ Error loading bound artists:', error)
                } finally {
                    setLoadingBoundArtists(false)
                }
            }
        } catch (error) {
            console.error('❌ Error loading artists:', error)
        } finally {
            setLoading(false)
        }
    }

    // Load bound artists for the current page
    const loadBoundArtists = async () => {
        if (!data || data.content.length === 0) {
            console.log('⚠️ No artists data to load bindings for')
            return
        }
        
        console.log('🔍 Loading bound artists...')
        setLoadingBoundArtists(true)
        try {
            // Extract all artist IDs
            const artistIds = data.content.map(artist => artist.id)
            console.log(`📋 Requesting bindings for ${artistIds.length} artists}`)
            
            // Fetch bound artists from music-data API
            const boundArtists = await fetchBoundArtists(artistIds)
            console.log(`✅ Bound artists loaded: ${boundArtists.length} items`)
            
            // Update the artists with bound information
            const updatedContent = data.content.map(artist => {
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
            
            console.log('📝 Updating artists with binding information')
            setData({ ...data, content: updatedContent })
        } catch (error) {
            console.error('❌ Error loading bound artists:', error)
        } finally {
            setLoadingBoundArtists(false)
        }
    }

    // Load artists when search parameters or pagination changes
    useEffect(() => {
        console.log('🔄 Effect triggered: page or sort changed')
        loadArtists()
    }, [page, sort])

    // Load bound artists when artist data changes
    useEffect(() => {
        console.log('🔄 Effect triggered: data content changed')
        if (!data || data.content.length === 0) return
        
        // Skip fetch if we just updated an artist through the UI
        if (skipNextFetchRef.current) {
            console.log('⏭️ Skipping bound artists fetch (skipNextFetchRef is true)')
            skipNextFetchRef.current = false
            return
        }
        
        // Skip fetch if only boundEntity property changed
        if (previousContentRef.current.length === data.content.length) {
            console.log('🔍 Checking if only boundEntity changed...')
            const onlyBoundEntityChanged = data.content.every((artist, index) => {
                const prevArtist = previousContentRef.current[index]
                // If IDs don't match, content has changed
                if (prevArtist.id !== artist.id) return false
                
                // If any property other than boundEntity changed, content has changed
                const artistWithoutBound = { ...artist }
                const prevArtistWithoutBound = { ...prevArtist }
                delete artistWithoutBound.boundEntity
                delete prevArtistWithoutBound.boundEntity
                
                return JSON.stringify(artistWithoutBound) === JSON.stringify(prevArtistWithoutBound)
            })
            
            if (onlyBoundEntityChanged) {
                console.log('⏭️ Skipping bound artists fetch (only boundEntity changed)')
                previousContentRef.current = [...data.content]
                return
            }
        }
        
        console.log('📝 Updating previousContentRef and loading bound artists')
        previousContentRef.current = [...data.content]
        loadBoundArtists()
    }, [data?.content])

    // Handle artist changes from child components
    const onArtistChanged = (updated: LastfmArtist) => {
        if (!data) return
        
        console.log(`🔄 Artist changed: (${updated.name})`)
        
        // Set flag to skip next fetch when we update an artist through the UI
        skipNextFetchRef.current = true
        
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
        skipNextFetchRef.current = false
        previousContentRef.current = []
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
