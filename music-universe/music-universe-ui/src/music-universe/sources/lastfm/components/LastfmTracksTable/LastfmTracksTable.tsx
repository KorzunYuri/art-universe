// hooks
import { useEffect, useState } from 'react'
// components
import {
    LastfmTracksTableHeader,
    LastfmTracksTableRow,
} from '@/music-universe/sources/lastfm/components'
// types
import type { LastfmTrack } from '@/music-universe/sources/lastfm/types/lastfm-track'
import type { Page } from '@/music-universe/shared/types/page'
// api
import { fetchTracks, type TrackSearchParams } from '@/music-universe/sources/lastfm/api/lastfm-tracks'
// styles
import commonStyles from '@/music-universe/shared/styles/common.module.scss'
import styles from './LastfmTracksTable.module.css'

interface LastfmTracksTableProps {
    artistId?: number;
}

export const LastfmTracksTable = ({ artistId }: LastfmTracksTableProps) => {
    // Search state
    const [searchName, setSearchName] = useState('')
    const [minPlayCount, setMinPlayCount] = useState<number | undefined>(undefined)
    const [minListenersCount, setMinListenersCount] = useState<number | undefined>(undefined)
    const [approvalStatuses, setApprovalStatuses] = useState<number[] | undefined>(undefined)
    
    // Data state
    const [data, setData] = useState<Page<LastfmTrack> | null>(null)
    const [loading, setLoading] = useState(false)
    const [page, setPage] = useState(0)
    const [sort, setSort] = useState('name,asc')
    const pageSize = 20
    
    // Load tracks with current search parameters
    const loadTracks = async () => {
        setLoading(true)
        try {
            const params: TrackSearchParams = {
                search: searchName || undefined,
                minPlayCount: minPlayCount,
                minListenersCount: minListenersCount,
                artistId: artistId,
                approvalStatuses: approvalStatuses,
                page,
                size: pageSize,
                sort
            }
            
            const result = await fetchTracks(params)
            console.log('📊 Tracks data received:', result)
            if (result.content && result.content.length > 0) {
                console.log('🎵 First track example:', result.content[0])
            }
            setData(result)
        } catch (error) {
            console.error('❌ Error loading tracks:', error)
        } finally {
            setLoading(false)
        }
    }

    // Load tracks when search parameters or pagination changes
    useEffect(() => {
        console.log('🔄 Effect triggered: page or sort changed')
        loadTracks()
    }, [page, sort, artistId])

    // Handle track changes from child components
    const onTrackChanged = (updated: LastfmTrack) => {
        if (!data) return
        
        console.log(`🔄 Track changed: (${updated.name})`)
        
        const newContent = data.content.map(t => t.id === updated.id ? updated : t)
        setData({ ...data, content: newContent })
    }

    // Handle search form submission
    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault()
        console.log('🔍 Search submitted')
        setPage(0) // Reset to first page when searching
        loadTracks()
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
        loadTracks()
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
                            placeholder="Search track name..."
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
                        <LastfmTracksTableHeader
                            sort={sort}
                            setSort={setSort}
                        />
                        {data.content.map((track) => (
                            <LastfmTracksTableRow
                                key={track.id}
                                track={track}
                                onChange={onTrackChanged}
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
