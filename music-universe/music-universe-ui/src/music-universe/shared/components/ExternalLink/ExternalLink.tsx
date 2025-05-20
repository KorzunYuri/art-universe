import './ExternalLink.module.css'

interface Props {
    href: string
    label: string
    className?: string
}

export function ExternalLink({ href, label, className = '' }: Props) {
    return (
        <a
            href={href}
            target="_blank"
            rel="noreferrer"
            className={`ext-link ${className}`}
        >
            {label}
        </a>
    )
}