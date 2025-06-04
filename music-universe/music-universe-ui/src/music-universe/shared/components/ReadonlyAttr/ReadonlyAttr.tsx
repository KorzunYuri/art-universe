import styles from './ReadonlyAttr.module.css'

interface Props {
    value: number
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