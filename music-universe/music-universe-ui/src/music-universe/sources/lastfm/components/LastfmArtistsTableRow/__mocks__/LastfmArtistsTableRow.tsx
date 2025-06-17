import React from 'react';

export const LastfmArtistsTableRow = ({ artist, onChange }: any) => {
  return (
    <div>
      <span>{artist.name}</span>
      {artist.boundArtist ? (
        <>
          <span>{artist.boundArtist.referenceName}</span>
          <button onClick={() => {}}>Unbind</button>
        </>
      ) : (
        <>
          <input defaultValue={artist.name} />
          <button onClick={() => {}}>Bind</button>
        </>
      )}
    </div>
  );
};
