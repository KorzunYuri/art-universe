import styles from './ReadonlyAttr.module.css'

interface Props {
    value: string | number | null
    className?: string
}

export function ReadonlyAttr({ value, className = '' }: Props) {
    return (
        <span
            className={`${styles.attrReadonly} ${className}`}
        >
            {value}
        </span>
    )
}