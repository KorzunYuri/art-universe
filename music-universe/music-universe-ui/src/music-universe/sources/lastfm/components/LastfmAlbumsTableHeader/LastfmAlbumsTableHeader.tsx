import styles from './LastfmAlbumsTableHeader.module.css';
import sharedStyles from "@/music-universe/shared/styles/EntityTableStyles.module.scss";
import albumStyles from "@/music-universe/sources/lastfm/components/LastfmAlbumsTable/LastfmAlbumsTable.module.css";

interface Props {
    sort?: string
    setSort: (value: string) => void
}

export const LastfmAlbumsTableHeader = ({ sort = '', setSort }: Props) => {
    const currentField = sort?.split(',')[0]
    const currentDir = sort?.split(',')[1] ?? 'asc'

    const toggleSort = (field: string) => {
        if (field === currentField) {
            const newDir = currentDir === 'asc' ? 'desc' : 'asc'
            setSort(`${field},${newDir}`)
        } else {
            setSort(`${field},desc`)
        }
    }

    const renderLabel = (label: string, field?: string) => {
        if (!field) return label
        const arrow = field === currentField ? (currentDir === 'asc' ? ' ▲' : ' ▼') : ''
        return label + arrow
    }

    return (
        <div className={`${styles.container} ${sharedStyles.header}`}>
            <div className={`${sharedStyles.cell} ${albumStyles.artist}`}>
                Artist
            </div>
            <div className={`${sharedStyles.cell} ${albumStyles.name}`} onClick={() => toggleSort('name')}>
                {renderLabel('Album name', 'name')}
            </div>
            <div className={`${sharedStyles.cell} ${albumStyles.mbid}`}>MusicBrainz</div>
            <div className={`${sharedStyles.cell} ${albumStyles.status}`}>Approval</div>
            <div className={`${sharedStyles.cell} ${albumStyles.masterBinding}`}>Master</div>
            <div className={`${sharedStyles.cell} ${albumStyles.count}`} onClick={() => toggleSort('playCount')}>
                {renderLabel('Plays', 'playCount')}
            </div>
            <div className={`${sharedStyles.cell} ${albumStyles.count}`} onClick={() => toggleSort('listenersCount')}>
                {renderLabel('Listeners', 'listenersCount')}
            </div>
        </div>
    )
}
