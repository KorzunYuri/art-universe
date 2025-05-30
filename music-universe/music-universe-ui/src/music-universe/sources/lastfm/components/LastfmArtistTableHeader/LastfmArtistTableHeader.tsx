import styles from './LastfmArtistTableHeader.module.css';
import sharedStyles from "../../../../shared/components/common/LastfmEntityTable.module.scss";
import artistStyles from "../LastfmArtistsTable/LastfmArtistsTable.module.css";

export const LastfmArtistTableHeader = () => {
  return (
      <div className={sharedStyles.header}>
          <div className={`${sharedStyles.cell} ${artistStyles.name}`}>Name</div>
          <div className={`${sharedStyles.cell} ${artistStyles.url}`}>Last.fm</div>
          <div className={`${sharedStyles.cell} ${artistStyles.mbid}`}>MusicBrainz</div>
          <div className={`${sharedStyles.cell} ${artistStyles.status}`}>Approval</div>
          <div className={`${sharedStyles.cell} ${artistStyles.count}`}>Plays</div>
          <div className={`${sharedStyles.cell} ${artistStyles.count}`}>Listeners</div>
      </div>
  );
};
