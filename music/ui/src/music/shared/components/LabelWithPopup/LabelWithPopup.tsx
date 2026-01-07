import styles from './LabelWithPopup.module.css'

interface Props {
    text: string
    popupText?: string
    className?: string
}

export function LabelWithPopup({ text, popupText, className = '' }: Props) {
    return (
        <div
            className={`${styles.label} ${className}`}
            title={popupText ?? text}
        >
            {text}
        </div>
    )
}