package yurykorzun.art.universe.music.quiz.service;

import yurykorzun.art.universe.music.quiz.dto.BindingDto;

import java.util.List;

public interface TrackService {
    
    BindingDto bind(Long masterId);
    
    BindingDto unbind(Long masterId);
    
    BindingDto getBinding(Long masterId);
    
    List<BindingDto> getBindings(List<Long> masterIds);
}
