package yurykorzun.art.universe.music.data.master.service;

import yurykorzun.art.universe.music.data.master.dto.attribute.AttributeDefDto;
import yurykorzun.art.universe.music.data.master.dto.attribute.AttributeDefSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeTargetType;

import java.util.List;

public interface AttributeDefService {

    List<AttributeDefDto> findAll();

    List<AttributeDefDto> findByTargetType(AttributeTargetType targetType);

    AttributeDefDto getById(Long id);

    AttributeDefDto create(AttributeDefSaveRequestDTO request);

    AttributeDefDto update(Long id, AttributeDefSaveRequestDTO request);

    boolean delete(Long id);
}
