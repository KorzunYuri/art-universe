interface LastfmArtistFilterButtonProps {
    artistId: number;
    artistName: string;
    onFilter: (artistId: number, artistName: string) => void;
    targetPage?: 'tracks' | 'albums';
}

export const LastfmArtistFilterButton = ({ 
    artistId, 
    artistName, 
    onFilter, 
    targetPage = 'tracks' 
}: LastfmArtistFilterButtonProps) => {
    const handleClick = (e: React.MouseEvent) => {
        e.stopPropagation();
        
        if (e.ctrlKey) {
            // Open target page for this artist in new window
            const url = `/lastfm/${targetPage}?artistId=${artistId}`;
            window.open(url, '_blank');
        } else {
            // Apply filter in current table
            onFilter(artistId, artistName);
        }
    };

    return (
        <button
            onClick={handleClick}
            title={`Filter ${targetPage} by ${artistName} (Ctrl+click to open in new window)`}
            style={{
                marginRight: '8px',
                padding: '2px 6px',
                fontSize: '12px',
                backgroundColor: '#f8f9fa',
                border: '1px solid #dee2e6',
                borderRadius: '3px',
                cursor: 'pointer',
                color: '#6c757d'
            }}
        >
            🔍
        </button>
    );
};
