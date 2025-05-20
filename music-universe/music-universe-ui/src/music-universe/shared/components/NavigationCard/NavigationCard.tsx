import { Link } from 'react-router-dom'
import styles from './NavigationCard.module.css'

interface Props {
    to: string
    label: string
}

export function NavigationCard({ to, label }: Props) {
    return (
        <Link
            to={to}
            className={styles.navCard}
        >
            {label}
        </Link>
    )
}
