import styles from './ApprovalToggle.module.scss'

const PENDING = 1
const APPROVED = 2
const DECLINED = 3
const AUTOAPPROVED = 4

type Status = typeof APPROVED | typeof DECLINED | typeof AUTOAPPROVED

interface Props {
    status: number
    onChange: (newStatus: number) => void
    className?: string
}

const options: {
    label: string
    value: Status
    color: 'yes' | 'no' | 'auto'
    disabled?: boolean
}[] = [
    { label: 'yes', value: APPROVED, color: 'yes' },
    { label: 'no', value: DECLINED, color: 'no' },
    { label: 'auto', value: AUTOAPPROVED, color: 'auto', disabled: true },
]

export function ApprovalToggle({ status, onChange, className = '' }: Props) {

    const handleClick = (value: number, disabled?: boolean) => {
        if (disabled) return
        if (value === status) {
            onChange(PENDING)
        } else {
            onChange(value)
        }
    }

    return (
        <div
            className={`${styles.toggleContainer} ${className}`}
        >
            {options.map(({ label, value, color, disabled }) => (
                <button
                    key={value}
                    type="button"
                    className={`${styles.button} ${styles[color]} ${
                        status === value ? styles.active : ''
                    }`}
                    disabled={disabled}
                    onClick={() => handleClick(value, disabled)}
                >
                    {label}
                </button>
            ))}
        </div>
    )
}