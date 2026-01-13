package yurykorzun.art.universe.music.data.raw.lastfm.etl.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiResponseService;

@RestController
@RequestMapping("/api/v1/api/responses")
@Slf4j
public class LastfmApiResponseController {

    public static final String SUB_URL_RESPONSE_BODY = "/body";

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

    @GetMapping(value = "/{id}" + SUB_URL_RESPONSE_BODY)
    public JsonNode getApiResponseBody(@PathVariable Long id) {
        return apiResponseService.getApiResponseBody(id);
    }
}
