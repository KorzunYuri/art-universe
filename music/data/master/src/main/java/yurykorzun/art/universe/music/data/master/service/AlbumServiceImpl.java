package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.master.dto.AlbumDto;
import yurykorzun.art.universe.music.data.master.dto.AlbumSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.AlbumWithCategoriesDto;
import yurykorzun.art.universe.music.data.master.dto.CategoryDto;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.Album;
import yurykorzun.art.universe.music.data.master.entity.AlbumCategory;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.repository.AlbumBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumCategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.master.service.lookup.ArtistRelatedLookupService;

import java.util.List;

@Service
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumBindingRepository bindingsRepository;
    private final AlbumCategoryRepository albumCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final ArtistRelatedLookupService lookupService;

    public AlbumServiceImpl(
        AlbumRepository albumRepository,
        AlbumBindingRepository bindingsRepository,
        AlbumCategoryRepository albumCategoryRepository,
        CategoryRepository categoryRepository,
        EntityManager entityManager
    ) {
        this.albumRepository = albumRepository;
        this.bindingsRepository = bindingsRepository;
        this.albumCategoryRepository = albumCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.ALBUM);
    }

    @Override
    public Page<AlbumDto> findAlbums(String search, Long categoryId, Pageable pageable) {
        if (categoryId == null) {
            return albumRepository.findAlbums(search, pageable)
                    .map(this::mapToDto);
        } else {
            return albumRepository.findAlbums(search, categoryId, pageable)
                    .map(this::mapToDto);
        }
    }

    @Override
    public Page<AlbumWithCategoriesDto> findAlbumsWithCategories(String search, Long categoryId, Pageable pageable) {
        return albumRepository.findAlbumsWithCategories(search, categoryId, pageable)
                .map(this::mapToAlbumWithCategories);
    }

    @Override
    public AlbumDto getAlbum(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException("Album", id));
        return mapToDto(album);
    }

    @Override
    public AlbumWithCategoriesDto getAlbumWithCategories(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException("Album", id));
        return mapToAlbumWithCategories(album);
    }

    @Override
    @Transactional
    public AlbumDto saveAlbum(AlbumSaveRequestDTO request) {
        Album album;
        if (request.getId() != null) {
            // Update existing album
            album = albumRepository.findById(request.getId())
                    .orElseThrow(() -> new CustomEntityNotFoundException("Album", request.getId()));
            album.setName(request.getName());
            if (request.getPrimaryArtistId() != null) {
                album.setPrimaryArtistId(request.getPrimaryArtistId());
            }
        } else {
            // Create new album — primaryArtistId is required
            if (request.getPrimaryArtistId() == null) {
                throw new IllegalArgumentException("primaryArtistId is required when creating a new album");
            }
            album = Album.builder()
                    .name(request.getName())
                    .primaryArtistId(request.getPrimaryArtistId())
                    .build();
        }

        Album savedAlbum = albumRepository.save(album);
        return mapToDto(savedAlbum);
    }

    @Override
    @Transactional
    public boolean deleteAlbum(Long id) {
        if (albumRepository.existsById(id)) {
            albumRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void bindToCategory(Long albumId, Long categoryId) {
        if (!albumRepository.existsById(albumId)) {
            throw new CustomEntityNotFoundException("Album", albumId);
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new CustomEntityNotFoundException("Category", categoryId);
        }
        if (albumCategoryRepository.existsByAlbumIdAndCategoryId(albumId, categoryId)) {
            throw new IllegalArgumentException("Relation between album " + albumId + " and category " + categoryId + " already exists");
        }

        AlbumCategory relation = AlbumCategory.builder()
            .albumId(albumId)
            .categoryId(categoryId)
            .build();
        albumCategoryRepository.save(relation);
    }

    @Override
    @Transactional
    public void unbindFromCategory(Long albumId, Long categoryId) {
        if (!albumRepository.existsById(albumId)) {
            throw new CustomEntityNotFoundException("Album", albumId);
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new CustomEntityNotFoundException("Category", categoryId);
        }

        AlbumCategory relation = albumCategoryRepository.findByAlbumIdAndCategoryId(albumId, categoryId)
            .orElseThrow(() -> new CustomEntityNotFoundException("Relation between album " + albumId + " and category " + categoryId + " not found"));
        albumCategoryRepository.delete(relation);
    }

    private AlbumDto mapToDto(Album album) {
        return AlbumDto.builder()
                .id(album.getId())
                .name(album.getName())
                .primaryArtistId(album.getPrimaryArtistId())
                .build();
    }

    private AlbumWithCategoriesDto mapToAlbumWithCategories(Album album) {
        List<CategoryDto> categories = List.of();
        if (album.getCategoryRelations() != null) {
            categories = album.getCategoryRelations().stream()
                    .map(relation -> CategoryDto.builder()
                            .id(relation.getCategory().getId())
                            .name(relation.getCategory().getName())
                            .build())
                    .toList();
        }

        return AlbumWithCategoriesDto.builder()
                .id(album.getId())
                .name(album.getName())
                .primaryArtistId(album.getPrimaryArtistId())
                .categories(categories)
                .build();
    }

    @Override
    public List<BoundEntityProjection> findBoundAlbums(DataSource dataSource, List<Long> externalIds) {
        return bindingsRepository.findBoundAlbumsForDataSource(dataSource, externalIds);
    }

    @Override
    public BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, ArtistRelatedEntityBindToExistingRequestDTO request) {
        throw new UnsupportedOperationException("Album binding is not yet implemented");
    }

    @Override
    public BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, ArtistRelatedEntityCreateAndBindRequestDTO request) {
        throw new UnsupportedOperationException("Album binding is not yet implemented");
    }

    @Override
    public boolean unbindAlbum(DataSource dataSource, Long externalId) {
        throw new UnsupportedOperationException("Album unbinding is not yet implemented");
    }

    @Override
    public List<LookupResultDTO> lookupAlbums(ArtistRelatedLookupRequestDTO request) {
        return lookupService.lookup(request);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchLookupResponseDTO batchLookupAlbums(ArtistRelatedBatchLookupRequestDTO request) {
        return lookupService.batchLookup(request);
    }
}
