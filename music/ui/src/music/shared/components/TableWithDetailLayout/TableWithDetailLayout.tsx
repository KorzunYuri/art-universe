import { type ReactNode, useState, useEffect } from 'react';
import { Outlet, useParams, useNavigate } from 'react-router-dom';
import { DetailPanel } from '../DetailPanel';
import styles from './TableWithDetailLayout.module.scss';

const ANIMATION_DURATION_MS = 200;

interface TableWithDetailLayoutProps {
    children: ReactNode;
    detailTitle?: string;
    /** The route param name that indicates a detail view is active (default: auto-detect) */
    detailParamName?: string;
}

/**
 * Layout component that renders a table alongside a sliding detail panel.
 * The detail panel is shown when a nested route with an ID param is active.
 *
 * Usage:
 * ```
 * <Route path="artists" element={<TableWithDetailLayout><ArtistsTable /></TableWithDetailLayout>}>
 *   <Route path=":artistId" element={<ArtistDetail />} />
 * </Route>
 * ```
 */
export const TableWithDetailLayout = ({
    children,
    detailTitle,
    detailParamName,
}: TableWithDetailLayoutProps) => {
    const params = useParams();
    const navigate = useNavigate();

    // Detect if a detail route is active by checking for any named param value.
    // Exclude the '*' wildcard param which comes from outer splat routes.
    const hasDetailRoute = detailParamName
        ? !!params[detailParamName]
        : Object.entries(params).some(([key, v]) => key !== '*' && v !== undefined && v !== '');

    // Keep the panel in the DOM during the exit animation.
    const [isRendered, setIsRendered] = useState(hasDetailRoute);
    const [isClosing, setIsClosing] = useState(false);

    useEffect(() => {
        if (hasDetailRoute) {
            setIsRendered(true);
            setIsClosing(false);
        } else if (isRendered) {
            setIsClosing(true);
            const timer = setTimeout(() => {
                setIsRendered(false);
                setIsClosing(false);
            }, ANIMATION_DURATION_MS);
            return () => clearTimeout(timer);
        }
    }, [hasDetailRoute]);

    const handleCloseDetail = () => {
        navigate('.', { relative: 'path' });
    };

    return (
        <div className={`${styles.layout} ${isRendered && !isClosing ? styles.withDetail : ''}`}>
            <div className={styles.tableSection}>
                {children}
            </div>
            {isRendered && (
                <div className={`${styles.detailSection} ${isClosing ? styles.closing : ''}`}>
                    <DetailPanel title={detailTitle} onClose={handleCloseDetail}>
                        <Outlet />
                    </DetailPanel>
                </div>
            )}
        </div>
    );
};
