// styles
import sharedStyles from "@/music-universe/shared/styles/EntityTableStyles.module.scss";
import artistStyles from "@/music-universe/sources/lastfm/components/LastfmArtistsTable/LastfmArtistsTable.module.css";
import styles from './LastfmArtistsTableHeader.module.css';

interface Props {
    sort?: string
    setSort: (value: string) => void
}

export const LastfmArtistsTableHeader = ({ sort = '', setSort }: Props) => {
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
            <div className={`${sharedStyles.cell} ${artistStyles.name}`} onClick={() => toggleSort('name')}>
                {renderLabel('Artist name', 'name')}
            </div>
            <div className={`${sharedStyles.cell} ${artistStyles.mbid}`}>MusicBrainz</div>
            <div className={`${sharedStyles.cell} ${artistStyles.status}`}>Approval</div>
            <div className={`${sharedStyles.cell} ${artistStyles.masterBinding}`}>Master</div>
            <div className={`${sharedStyles.cell} ${artistStyles.quizBinding}`}>Quiz</div>
            <div className={`${sharedStyles.cell} ${artistStyles.count}`} onClick={() => toggleSort('playCount')}>
                {renderLabel('Plays', 'playCount')}
            </div>
            <div className={`${sharedStyles.cell} ${artistStyles.count}`} onClick={() => toggleSort('listenersCount')}>
                {renderLabel('Listeners', 'listenersCount')}
            </div>
        </div>
    )
}
