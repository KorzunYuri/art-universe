import { NavLink } from 'react-router-dom';
import { useAuth } from '@/shared/hooks/useAuth';
import { usePermissions } from '@/shared/hooks/usePermissions';
import type { AccessLevel } from '@/shared/types/access';
import styles from './AppHeader.module.css';

interface NavChild {
    label: string;
    to: string;
    access?: AccessLevel;
}

interface NavItem {
    label: string;
    to: string;
    access?: AccessLevel;
    children: NavChild[];
}

export function AppHeader() {
    const { user, logout } = useAuth();
    const permissions = usePermissions();

    const navItems: NavItem[] = [
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
                { label: 'Admin',   to: '/music/data/raw/lastfm/admin', access: permissions.adminAccess },
            ],
        },
        {
            label: 'Spotify',
            to: '/music/data/raw/spotify',
            children: [
                { label: 'Artists', to: '/music/data/raw/spotify/artists' },
                { label: 'Albums',  to: '/music/data/raw/spotify/albums' },
                { label: 'Tracks',  to: '/music/data/raw/spotify/tracks' },
                { label: 'Genres',  to: '/music/data/raw/spotify/genres' },
            ],
        },
        {
            label: 'Quiz',
            to: '/music/quiz',
            access: permissions.quizAccess,
            children: [
                { label: 'Games', to: '/music/quiz' },
            ],
        },
        {
            label: 'Config',
            to: '/common/config',
            access: permissions.configAccess,
            children: [],
        },
    ];

    const visibleItems = navItems.filter(item => item.access !== 'hidden');

    return (
        <header className={styles.header}>
            <NavLink end to="/" className={({ isActive }) =>
                isActive ? `${styles.logo} ${styles.logoActive}` : styles.logo
            }>
                Art Universe
            </NavLink>

            <nav className={styles.nav}>
                {visibleItems.map((item) => {
                    const visibleChildren = item.children.filter(child => child.access !== 'hidden');
                    return (
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
                                {visibleChildren.map((child) => (
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
                    );
                })}
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
