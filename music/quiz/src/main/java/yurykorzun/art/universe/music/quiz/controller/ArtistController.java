package yurykorzun.art.universe.music.quiz.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.quiz.dto.BindingDto;
import yurykorzun.art.universe.music.quiz.service.ArtistService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
@Slf4j
public class ArtistController {

    private final ArtistService artistService;

    @PostMapping("/{masterId}/bind")
    public BindingDto bind(@PathVariable Long masterId) {
        log.debug("Binding artist with masterId: {}", masterId);
        return artistService.bind(masterId);
    }

    @DeleteMapping("/{masterId}/bind")
    public BindingDto unbind(@PathVariable Long masterId) {
        log.debug("Unbinding artist with masterId: {}", masterId);
        return artistService.unbind(masterId);
    }

    @GetMapping("/{masterId}/binding")
    public BindingDto getBinding(@PathVariable Long masterId) {
        log.debug("Getting binding for artist with masterId: {}", masterId);
        return artistService.getBinding(masterId);
    }

    @PostMapping("/bindings")
    public List<BindingDto> getBindings(@RequestBody List<Long> masterIds) {
        log.debug("Getting bindings for {} artist masterIds", masterIds.size());
        return artistService.getBindings(masterIds);
    }
}
