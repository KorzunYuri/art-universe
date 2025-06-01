import styles from './LastfmArtistsTableHeader.module.css';
import sharedStyles from "@/music-universe/sources/lastfm/common/LastfmEntityTable.module.scss";
import artistStyles from "@/music-universe/sources/lastfm/components/LastfmArtistsTable/LastfmArtistsTable.module.css";

export const LastfmArtistsTableHeader = () => {
  return (
      <div
          className={`${styles.container} ${sharedStyles.header}`}
      >
          <div className={`${sharedStyles.cell} ${artistStyles.name}`}>Name</div>
          <div className={`${sharedStyles.cell} ${artistStyles.url}`}>Last.fm</div>
          <div className={`${sharedStyles.cell} ${artistStyles.mbid}`}>MusicBrainz</div>
          <div className={`${sharedStyles.cell} ${artistStyles.status}`}>Approval</div>
          <div className={`${sharedStyles.cell} ${artistStyles.count}`}>Plays</div>
          <div className={`${sharedStyles.cell} ${artistStyles.count}`}>Listeners</div>
      </div>
  );
};
