import styles from './DimensionsTableHeader.module.css';
import sharedStyles from "@/music-universe/shared/components/BaseEntityTable/EntityTableStyles.module.scss";

interface Props {
    sort: string
    setSort: (value: string) => void
}

export const DimensionsTableHeader = ({ sort, setSort }: Props) => {
    const currentField = sort?.split(',')[0]
    const currentDir = sort?.split(',')[1] ?? 'asc'

    const toggleSort = (field: string) => {
        if (field === currentField) {
            const newDir = currentDir === 'asc' ? 'desc' : 'asc'
            setSort(`${field},${newDir}`)
        } else {
            setSort(`${field},asc`)
        }
    }

    const renderLabel = (label: string, field?: string) => {
        if (!field) return label
        const arrow = field === currentField ? (currentDir === 'asc' ? ' ▲' : ' ▼') : ''
        return label + arrow
    }

    return (
        <div className={`${styles.container} ${sharedStyles.header}`}>
            <div className={`${sharedStyles.cell} ${styles.name}`} onClick={() => toggleSort('name')}>
                {renderLabel('Name', 'name')}
            </div>
            <div className={`${sharedStyles.cell} ${styles.actions}`}>
                Actions
            </div>
        </div>
    )
}
