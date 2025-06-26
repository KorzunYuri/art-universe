import { LastfmTagsTable } from "@/music-universe/sources/lastfm/components";
import styles from './LastfmTags.module.css';

export const LastfmTags = () => {
  return (
    <div className={styles.page}>
      <h2>Tags Page</h2>
      <LastfmTagsTable />
    </div>
  );
};
