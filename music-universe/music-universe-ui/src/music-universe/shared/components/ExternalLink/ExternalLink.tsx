import styles from './ExternalLink.module.css'

interface Props {
    href: string
    label: string
    className?: string
}

export function ExternalLink({ href, label, className = '' }: Props) {
    return (
        <a
            className={`${styles.extLink} ${className}`}
            href={href}
            target="_blank"
            rel="noreferrer"
            onClick={(e) => e.stopPropagation()}
        >
            {label}
        </a>
    )
}