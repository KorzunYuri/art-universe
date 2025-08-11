package yurykorzun.art.universe.music.data.raw.lastfm.api.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;

@RestController
@RequestMapping("/api/v1/api/responses")
@Slf4j
public class LastfmApiResponseController {

    private final LastfmApiResponseService apiResponseService;

    public LastfmApiResponseController(LastfmApiResponseService apiResponseService) {
        this.apiResponseService = apiResponseService;
    }

    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LastfmApiResponseDto getApiResponse(@PathVariable Long id) {
        return apiResponseService.getApiResponseById(id);
    }
}
