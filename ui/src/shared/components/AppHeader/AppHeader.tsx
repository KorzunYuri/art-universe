import { NavLink } from 'react-router-dom';
import { useAuth } from '@/shared/hooks/useAuth';
import styles from './AppHeader.module.css';

const NAV_ITEMS = [
    {
        label: 'Art Master',
        to: '/art/data/master',
        children: [
            { label: 'Persons', to: '/art/data/master/persons' },
        ],
    },
    {
        label: 'Music Master',
        to: '/music/data/master',
        children: [
            { label: 'Categories',     to: '/music/data/master/categories' },
            { label: 'Artists',        to: '/music/data/master/artists' },
            { label: 'Albums',         to: '/music/data/master/albums' },
            { label: 'Tracks',         to: '/music/data/master/tracks' },
            { label: 'Relation Types', to: '/music/data/master/relation-types' },
        ],
    },
    {
        label: 'Lastfm',
        to: '/music/data/raw/lastfm',
        children: [
            { label: 'Tags',    to: '/music/data/raw/lastfm/tags' },
            { label: 'Artists', to: '/music/data/raw/lastfm/artists' },
            { label: 'Albums',  to: '/music/data/raw/lastfm/albums' },
            { label: 'Tracks',  to: '/music/data/raw/lastfm/tracks' },
            { label: 'Admin',   to: '/music/data/raw/lastfm/admin' },
        ],
    },

    {
        label: 'Quiz',
        to: '/music/quiz',
        children: [
            { label: 'Games', to: '/music/quiz' },
        ],
    },
] as const;

export function AppHeader() {
    const { user, logout } = useAuth();

    return (
        <header className={styles.header}>
            <NavLink end to="/" className={({ isActive }) =>
                isActive ? `${styles.logo} ${styles.logoActive}` : styles.logo
            }>
                Art Universe
            </NavLink>

            <nav className={styles.nav}>
                {NAV_ITEMS.map((item) => (
                    <div key={item.to} className={styles.navSection}>
                        <NavLink
                            to={item.to}
                            className={({ isActive }) =>
                                isActive ? `${styles.domainLink} ${styles.domainLinkActive}` : styles.domainLink
                            }
                        >
                            {item.label}
                        </NavLink>

                        <div className={styles.subLinks}>
                            {item.children.map((child) => (
                                <NavLink
                                    key={child.to}
                                    to={child.to}
                                    className={({ isActive }) =>
                                        isActive ? `${styles.subLink} ${styles.subLinkActive}` : styles.subLink
                                    }
                                >
                                    {child.label}
                                </NavLink>
                            ))}
                        </div>
                    </div>
                ))}
            </nav>

            {user && (
                <div className={styles.userSection}>
                    {user.pictureUrl && (
                        <img
                            src={user.pictureUrl}
                            alt={user.name}
                            className={styles.avatar}
                            referrerPolicy="no-referrer"
                        />
                    )}
                    <span className={styles.userName}>{user.name}</span>
                    <button onClick={logout} className={styles.logoutButton}>
                        Sign out
                    </button>
                </div>
            )}
        </header>
    );
}
