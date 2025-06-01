import { Link } from 'react-router-dom'
import styles from './NavigationCard.module.css'

interface Props {
    to: string
    label: string
    className?: string
}

export function NavigationCard({ to, label, className = '' }: Props) {
    return (
        <Link
            to={to}
            className={`${styles.navCard} ${className}`}
        >
            {label}
        </Link>
    )
}
