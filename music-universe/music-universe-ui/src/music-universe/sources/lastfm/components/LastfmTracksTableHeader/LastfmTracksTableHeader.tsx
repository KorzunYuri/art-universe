import styles from './LastfmTracksTableHeader.module.css';
import sharedStyles from "@/music-universe/shared/components/BaseEntityTable/EntityTableStyles.module.scss";
import trackStyles from "@/music-universe/sources/lastfm/components/LastfmTracksTable/LastfmTracksTable.module.css";

interface Props {
    sort?: string
    setSort: (value: string) => void
}

export const LastfmTracksTableHeader = ({ sort = '', setSort }: Props) => {
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
            <div className={`${sharedStyles.cell} ${trackStyles.artist}`}>
                Artist
            </div>
            <div className={`${sharedStyles.cell} ${trackStyles.name}`} onClick={() => toggleSort('name')}>
                {renderLabel('Track name', 'name')}
            </div>
            <div className={`${sharedStyles.cell} ${trackStyles.mbid}`}>MusicBrainz</div>
            <div className={`${sharedStyles.cell} ${trackStyles.status}`}>Approval</div>
            <div className={`${sharedStyles.cell} ${trackStyles.binding}`}>Music Data</div>
            <div className={`${sharedStyles.cell} ${trackStyles.count}`} onClick={() => toggleSort('playCount')}>
                {renderLabel('Plays', 'playCount')}
            </div>
            <div className={`${sharedStyles.cell} ${trackStyles.count}`} onClick={() => toggleSort('listenersCount')}>
                {renderLabel('Listeners', 'listenersCount')}
            </div>
        </div>
    )
}
