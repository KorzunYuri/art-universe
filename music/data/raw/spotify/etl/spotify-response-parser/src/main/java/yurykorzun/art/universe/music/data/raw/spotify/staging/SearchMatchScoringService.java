package yurykorzun.art.universe.music.data.raw.spotify.staging;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SearchAttemptStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifySearchAttempt;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifySearchAttemptRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class SearchMatchScoringService {

    private final SpotifySearchAttemptRepository searchAttemptRepository;
    private final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();

    @Value("${spotify.search.match-threshold:0.85}")
    private double matchThreshold;

    @Value("${spotify.search.grace-period-days:30}")
    private int gracePeriodDays;

    public SearchMatchScoringService(SpotifySearchAttemptRepository searchAttemptRepository) {
        this.searchAttemptRepository = searchAttemptRepository;
    }

    /**
     * Scores search results against the search_attempt's search string using Jaro-Winkler similarity.
     * Updates the search_attempt status to MATCHED or NO_MATCH.
     *
     * @param apiCallId          the api_call ID linked to this search
     * @param candidateNames     list of candidate entity names from search results
     * @param candidateSpotifyIds list of Spotify IDs corresponding to each candidate
     */
    public void scoreAndUpdate(long apiCallId, List<String> candidateNames, List<String> candidateSpotifyIds) {
        SpotifySearchAttempt attempt = searchAttemptRepository.findByApiCallId(apiCallId).orElse(null);
        if (attempt == null) {
            log.warn("No search_attempt found for api_call_id={}, skipping match scoring", apiCallId);
            return;
        }

        String searchString = attempt.getSearchString().toLowerCase();
        double bestScore = 0.0;
        String bestSpotifyId = null;

        for (int i = 0; i < candidateNames.size(); i++) {
            String candidateName = candidateNames.get(i).toLowerCase();
            double score = jaroWinkler.apply(searchString, candidateName);
            if (score > bestScore) {
                bestScore = score;
                bestSpotifyId = candidateSpotifyIds.get(i);
            }
        }

        attempt.setBestMatchScore(bestScore);
        attempt.setSearchedAt(Instant.now());
        attempt.setUpdatedAt(Instant.now());

        if (bestScore >= matchThreshold && bestSpotifyId != null) {
            attempt.setStatus(SearchAttemptStatus.MATCHED);
            attempt.setMatchedSpotifyId(bestSpotifyId);
            log.info("Search match found for master entity {} (type={}): spotifyId={}, score={}",
                attempt.getMasterEntityId(), attempt.getEntityType(), bestSpotifyId, bestScore);
        } else {
            attempt.setStatus(SearchAttemptStatus.NO_MATCH);
            attempt.setNextRetryAfter(Instant.now().plus(gracePeriodDays, ChronoUnit.DAYS));
            log.info("No match found for master entity {} (type={}): bestScore={}",
                attempt.getMasterEntityId(), attempt.getEntityType(), bestScore);
        }

        searchAttemptRepository.save(attempt);
    }
}
