package yurykorzun.art.universe.music.data.master.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.master.common.archetypes.BaseMasterDataJpaTest;
import yurykorzun.art.universe.music.data.master.dto.CategoryDagDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryDagNodeDTO;
import yurykorzun.art.universe.music.data.master.entity.*;
import yurykorzun.art.universe.music.data.master.repository.*;

import static org.assertj.core.api.Assertions.assertThat;

@Import({
        CategoryServiceImpl.class
})
class CategoryServiceHierarchyCountsTest extends BaseMasterDataJpaTest {

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CategoryCategoryRepository categoryCategoryRepository;
    
    @Autowired
    private ArtistRepository artistRepository;
    
    @Autowired
    private TrackRepository trackRepository;
    
    @Autowired
    private ArtistCategoryRepository artistCategoryRepository;
    
    @Autowired
    private ArtistTrackRepository artistTrackRepository;

    private Category music, rock, metal, alternative, heavyMetal, progressiveRock;
    private Artist artist1, artist2, artist3;
    private Track track1, track2, track3, track4;

    @BeforeEach
    void setUp() {

    }

    @Test
    void getCategoryDag_shouldCalculateHierarchicalCounts() {
        // given
        // Create hierarchy: Music -> Rock -> Metal -> Heavy Metal
        //                         -> Alternative
        //                         -> Progressive Rock (diamond: also child of Metal)

        music = categoryRepository.save(Category.builder().name("Music").build());
        rock = categoryRepository.save(Category.builder().name("Rock").build());
        metal = categoryRepository.save(Category.builder().name("Metal").build());
        alternative = categoryRepository.save(Category.builder().name("Alternative").build());
        heavyMetal = categoryRepository.save(Category.builder().name("Heavy Metal").build());
        progressiveRock = categoryRepository.save(Category.builder().name("Progressive Rock").build());

        // Create relations (diamond structure)
        categoryCategoryRepository.save(CategoryCategory.builder()
                .sourceCategoryId(music.getId()).targetCategoryId(rock.getId()).build());
        categoryCategoryRepository.save(CategoryCategory.builder()
                .sourceCategoryId(rock.getId()).targetCategoryId(metal.getId()).build());
        categoryCategoryRepository.save(CategoryCategory.builder()
                .sourceCategoryId(rock.getId()).targetCategoryId(alternative.getId()).build());
        categoryCategoryRepository.save(CategoryCategory.builder()
                .sourceCategoryId(metal.getId()).targetCategoryId(heavyMetal.getId()).build());
        categoryCategoryRepository.save(CategoryCategory.builder()
                .sourceCategoryId(rock.getId()).targetCategoryId(progressiveRock.getId()).build());
        categoryCategoryRepository.save(CategoryCategory.builder()
                .sourceCategoryId(metal.getId()).targetCategoryId(progressiveRock.getId()).build()); // Diamond!

        // Create artists
        artist1 = artistRepository.save(Artist.builder().name("Metallica").build());
        artist2 = artistRepository.save(Artist.builder().name("Dream Theater").build());
        artist3 = artistRepository.save(Artist.builder().name("Nirvana").build());

        // Create tracks
        track1 = trackRepository.save(Track.builder().name("Master of Puppets").primaryArtistId(artist1.getId()).build());
        track2 = trackRepository.save(Track.builder().name("Pull Me Under").primaryArtistId(artist2.getId()).build());
        track3 = trackRepository.save(Track.builder().name("Smells Like Teen Spirit").primaryArtistId(artist3.getId()).build());
        track4 = trackRepository.save(Track.builder().name("Another Metallica Song").primaryArtistId(artist1.getId()).build());

        // Link artists to categories
        artistCategoryRepository.save(ArtistCategory.builder()
                .artistId(artist1.getId()).categoryId(heavyMetal.getId()).build()); // Metallica -> Heavy Metal
        artistCategoryRepository.save(ArtistCategory.builder()
                .artistId(artist2.getId()).categoryId(progressiveRock.getId()).build()); // Dream Theater -> Progressive Rock
        artistCategoryRepository.save(ArtistCategory.builder()
                .artistId(artist3.getId()).categoryId(alternative.getId()).build()); // Nirvana -> Alternative

        // Link artists to tracks
        artistTrackRepository.save(ArtistTrack.builder()
                .artistId(artist1.getId()).trackId(track1.getId()).build());
        artistTrackRepository.save(ArtistTrack.builder()
                .artistId(artist1.getId()).trackId(track4.getId()).build());
        artistTrackRepository.save(ArtistTrack.builder()
                .artistId(artist2.getId()).trackId(track2.getId()).build());
        artistTrackRepository.save(ArtistTrack.builder()
                .artistId(artist3.getId()).trackId(track3.getId()).build());


        // When
        CategoryDagDTO result = categoryService.getCategoryDag();


        // Then
        assertThat(result.getNodes()).hasSize(6);

        // Find nodes by name for easier testing
        CategoryDagNodeDTO musicNode = findNodeByName(result, "Music");
        CategoryDagNodeDTO rockNode = findNodeByName(result, "Rock");
        CategoryDagNodeDTO metalNode = findNodeByName(result, "Metal");
        CategoryDagNodeDTO alternativeNode = findNodeByName(result, "Alternative");
        CategoryDagNodeDTO heavyMetalNode = findNodeByName(result, "Heavy Metal");
        CategoryDagNodeDTO progressiveRockNode = findNodeByName(result, "Progressive Rock");

        // Verify hierarchy structure
        assertThat(musicNode.isRoot()).isTrue();
        assertThat(rockNode.isRoot()).isFalse();
        assertThat(metalNode.isRoot()).isFalse();
        assertThat(alternativeNode.isRoot()).isFalse();
        assertThat(heavyMetalNode.isRoot()).isFalse();
        assertThat(progressiveRockNode.isRoot()).isFalse();

        // Verify children counts (all descendants)
        assertThat(musicNode.getChildrenCount()).isEqualTo(5); // All categories except itself
        assertThat(rockNode.getChildrenCount()).isEqualTo(4); // Metal, Alternative, Heavy Metal, Progressive Rock
        assertThat(metalNode.getChildrenCount()).isEqualTo(2); // Heavy Metal, Progressive Rock
        assertThat(alternativeNode.getChildrenCount()).isEqualTo(0); // No children
        assertThat(heavyMetalNode.getChildrenCount()).isEqualTo(0); // No children
        assertThat(progressiveRockNode.getChildrenCount()).isEqualTo(0); // No children

        // Verify artists counts (distinct artists in all descendants)
        assertThat(musicNode.getArtistsCount()).isEqualTo(3); // All artists: Metallica, Dream Theater, Nirvana
        assertThat(rockNode.getArtistsCount()).isEqualTo(3); // All artists: Metallica, Dream Theater, Nirvana
        assertThat(metalNode.getArtistsCount()).isEqualTo(2); // Metallica (Heavy Metal), Dream Theater (Progressive Rock)
        assertThat(alternativeNode.getArtistsCount()).isEqualTo(1); // Nirvana
        assertThat(heavyMetalNode.getArtistsCount()).isEqualTo(1); // Metallica
        assertThat(progressiveRockNode.getArtistsCount()).isEqualTo(1); // Dream Theater

        // Verify tracks counts (distinct tracks through artists in all descendants)
        assertThat(musicNode.getTracksCount()).isEqualTo(4); // All tracks
        assertThat(rockNode.getTracksCount()).isEqualTo(4); // All tracks
        assertThat(metalNode.getTracksCount()).isEqualTo(3); // Metallica's 2 tracks + Dream Theater's 1 track
        assertThat(alternativeNode.getTracksCount()).isEqualTo(1); // Nirvana's 1 track
        assertThat(heavyMetalNode.getTracksCount()).isEqualTo(2); // Metallica's 2 tracks
        assertThat(progressiveRockNode.getTracksCount()).isEqualTo(1); // Dream Theater's 1 track
    }

    private CategoryDagNodeDTO findNodeByName(CategoryDagDTO dag, String name) {
        return dag.getNodes().stream()
            .filter(node -> name.equals(node.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Node not found: " + name));
    }
}
