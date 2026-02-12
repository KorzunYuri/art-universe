import type { ReactNode } from 'react';
import { Outlet, useParams, useNavigate } from 'react-router-dom';
import { DetailPanel } from '../DetailPanel';
import styles from './TableWithDetailLayout.module.scss';

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

    // Detect if a detail route is active by checking for any param value
    const hasDetailRoute = detailParamName
        ? !!params[detailParamName]
        : Object.values(params).some(v => v !== undefined && v !== '');

    const handleCloseDetail = () => {
        navigate('.', { relative: 'path' });
    };

    return (
        <div className={`${styles.layout} ${hasDetailRoute ? styles.withDetail : ''}`}>
            <div className={styles.tableSection}>
                {children}
            </div>
            {hasDetailRoute && (
                <div className={styles.detailSection}>
                    <DetailPanel title={detailTitle} onClose={handleCloseDetail}>
                        <Outlet />
                    </DetailPanel>
                </div>
            )}
        </div>
    );
};
