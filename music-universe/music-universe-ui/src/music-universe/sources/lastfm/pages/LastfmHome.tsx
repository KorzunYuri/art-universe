import NavigationCard from '../../../shared/components/NavigationCard'

export default function LastfmHome() {
    return (
        <div>
            <h2>Last.fm</h2>
            <NavigationCard to="/lastfm/tags"       label="Tags" />
            <NavigationCard to="/lastfm/artists"    label="Artists" />
            <NavigationCard to="/lastfm/albums"     label="Albums" />
            <NavigationCard to="/lastfm/tracks"     label="Tracks" />
        </div>
    )
}