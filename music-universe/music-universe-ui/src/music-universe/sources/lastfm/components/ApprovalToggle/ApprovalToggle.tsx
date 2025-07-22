import styles from './ApprovalToggle.module.scss'
import { ApprovalStatus, type ApprovalStatusType } from '@/music-universe/sources/lastfm/constants/approvalStatus'

interface Props {
    status: number
    onChange: (newStatus: ApprovalStatusType) => void
    className?: string
    disabled?: boolean
}

const options: {
    label: string
    value: ApprovalStatusType
    color: 'yes' | 'no' | 'auto'
    alwaysDisabled?: boolean
}[] = [
    { label: 'yes',  value: ApprovalStatus.APPROVED,        color: 'yes' },
    { label: 'no',   value: ApprovalStatus.DECLINED,        color: 'no' },
    { label: 'auto', value: ApprovalStatus.AUTOAPPROVED,    color: 'auto', alwaysDisabled: true },
]

export function ApprovalToggle({ status, onChange, className = '', disabled = false }: Props) {

    const handleClick = (value: ApprovalStatusType, buttonDisabled?: boolean) => {
        if (buttonDisabled || disabled) return
        if (value === status) {
            onChange(ApprovalStatus.PENDING)
        } else {
            onChange(value)
        }
    }

    return (
        <div
            className={`${styles.toggleContainer} ${className}`}
        >
            {options.map(({ label, value, color, alwaysDisabled }) => (
                <button
                    key={value}
                    type="button"
                    className={`${styles.button} ${styles[color]} ${
                        status === value ? styles.active : ''
                    }`}
                    disabled={alwaysDisabled || disabled}
                    onClick={() => handleClick(value, alwaysDisabled)}
                >
                    {label}
                </button>
            ))}
        </div>
    )
}
