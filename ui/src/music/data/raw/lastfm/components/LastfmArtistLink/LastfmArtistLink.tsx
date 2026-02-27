import React from 'react';

interface ArtistLinkProps {
    artistName: string;
}

export const LastfmArtistLink = ({ artistName }: ArtistLinkProps) => {
    const handleClick = (e: React.MouseEvent) => {
        e.stopPropagation();
        const url = `/music/data/raw/lastfm/artists?search=${encodeURIComponent(artistName)}`;
        window.open(url, '_blank');
    };

    return (
        <span 
            onClick={handleClick}
            style={{ 
                color: '#007bff', 
                cursor: 'pointer', 
                textDecoration: 'underline' 
            }}
        >
            {artistName}
        </span>
    );
};
