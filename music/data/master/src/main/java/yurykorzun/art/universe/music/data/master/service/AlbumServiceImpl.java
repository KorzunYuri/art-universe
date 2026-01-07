package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.repository.AlbumBindingRepository;
import yurykorzun.art.universe.music.data.master.service.lookup.ArtistRelatedLookupService;

import java.util.List;

@Service
public class AlbumServiceImpl implements AlbumService {

    private final AlbumBindingRepository bindingsRepository;
    private final ArtistRelatedLookupService lookupService;

    public AlbumServiceImpl(
        AlbumBindingRepository bindingsRepository,
        EntityManager entityManager
    ) {
        this.bindingsRepository = bindingsRepository;
        this.lookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.ALBUM);
    }

    @Override
    public List<BoundEntityProjection> findBoundAlbums(DataSource dataSource, List<Long> externalIds) {
        return bindingsRepository.findBoundAlbumsForDataSource(dataSource, externalIds);
    }

    @Override
    public BoundEntityProjection bindToExisting(DataSource dataSource, Long externalId, ArtistRelatedEntityBindToExistingRequestDTO request) {
        // This method will be implemented in the future
        throw new UnsupportedOperationException("Album binding is not yet implemented");
    }

    @Override
    public BoundEntityProjection createAndBind(DataSource dataSource, Long externalId, ArtistRelatedEntityCreateAndBindRequestDTO request) {
        // This method will be implemented in the future
        throw new UnsupportedOperationException("Album binding is not yet implemented");
    }

    @Override
    public boolean unbindAlbum(DataSource dataSource, Long externalId) {
        // This method will be implemented in the future
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
