import { Link } from 'react-router-dom'
import './NavigationCard.css'

interface Props {
    to: string
    label: string
}

export default function NavigationCard({ to, label }: Props) {
    return (
        <Link
            to={to}
            className='nav-card'
        >
            {label}
        </Link>
    )
}
