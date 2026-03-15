import { useQuery } from '@tanstack/react-query';
import { fetchAllProperties } from '../../api/config-api';
import { PropertiesTree } from '../../components/PropertiesTree/PropertiesTree';
import styles from './ConfigProperties.module.css';

export function ConfigProperties() {
    const { data, isLoading, isError, refetch } = useQuery({
        queryKey: ['config-properties'],
        queryFn: fetchAllProperties,
    });

    if (isLoading) return <div className={styles.status}>Loading properties…</div>;
    if (isError) return (
        <div className={styles.status}>
            Failed to load properties.{' '}
            <button className={styles.retryButton} onClick={() => refetch()}>Retry</button>
        </div>
    );

    return (
        <div className={styles.page}>
            <div className={styles.header}>
                <h2>Configuration Properties</h2>
                <button className={styles.refreshButton} onClick={() => refetch()}>Refresh</button>
            </div>
            <PropertiesTree properties={data ?? []} />
        </div>
    );
}
