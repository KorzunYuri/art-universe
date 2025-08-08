// styles
import sharedStyles from "@/music-universe/shared/styles/EntityTableStyles.module.scss";
import tagStyles from "@/music-universe/sources/lastfm/components/LastfmTagsTable/LastfmTagsTable.module.css";
import styles from './LastfmTagsTableHeader.module.css';

interface Props {
    sort?: string
    setSort: (value: string) => void
}

export const LastfmTagsTableHeader = ({ sort = '', setSort }: Props) => {
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
            <div className={`${sharedStyles.cell} ${tagStyles.name}`} onClick={() => toggleSort('name')}>
                {renderLabel('Tag Name', 'name')}
            </div>
            <div className={`${sharedStyles.cell} ${tagStyles.status}`}>Approval</div>
            <div className={`${sharedStyles.cell} ${tagStyles.masterBinding}`}>Master</div>
            <div className={`${sharedStyles.cell} ${tagStyles.count}`} onClick={() => toggleSort('usageCount')}>
                {renderLabel('Usage', 'usageCount')}
            </div>
            <div className={`${sharedStyles.cell} ${tagStyles.count}`} onClick={() => toggleSort('usageUsersCount')}>
                {renderLabel('Users', 'usageUsersCount')}
            </div>
        </div>
    )
}
