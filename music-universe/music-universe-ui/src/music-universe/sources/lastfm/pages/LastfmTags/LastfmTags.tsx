import { useLocation } from 'react-router-dom';
import { LastfmTagsTable } from "@/music-universe/sources/lastfm/components";
import styles from './LastfmTags.module.css';

export const LastfmTags = () => {
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const initialSearch = searchParams.get('search') || '';
  
  return (
    <div className={styles.page}>
      <h2>Tags Page</h2>
      <LastfmTagsTable initialSearch={initialSearch} />
    </div>
  );
};
