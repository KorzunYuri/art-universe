package yurykorzun.art.universe.music.quiz.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.quiz.dto.BindingDto;
import yurykorzun.art.universe.music.quiz.service.TrackService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tracks")
@RequiredArgsConstructor
@Slf4j
public class TrackController {

    private final TrackService trackService;

    @PostMapping("/{masterId}/bind")
    public BindingDto bind(@PathVariable Long masterId) {
        log.debug("Binding track with masterId: {}", masterId);
        return trackService.bind(masterId);
    }

    @DeleteMapping("/{masterId}/bind")
    public BindingDto unbind(@PathVariable Long masterId) {
        log.debug("Unbinding track with masterId: {}", masterId);
        return trackService.unbind(masterId);
    }

    @GetMapping("/{masterId}/binding")
    public BindingDto getBinding(@PathVariable Long masterId) {
        log.debug("Getting binding for track with masterId: {}", masterId);
        return trackService.getBinding(masterId);
    }

    @PostMapping("/bindings")
    public List<BindingDto> getBindings(@RequestBody List<Long> masterIds) {
        log.debug("Getting bindings for {} track masterIds", masterIds.size());
        return trackService.getBindings(masterIds);
    }
}
