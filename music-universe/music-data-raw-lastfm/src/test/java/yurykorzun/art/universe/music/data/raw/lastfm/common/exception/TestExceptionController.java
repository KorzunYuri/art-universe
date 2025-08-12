package yurykorzun.art.universe.music.data.raw.lastfm.common.exception;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import yurykorzun.art.universe.common.exception.EntityNotFoundException;

@RestController
public class TestExceptionController {
    
    @GetMapping("/test/entity-not-found/{id}")
    public String throwEntityNotFoundException(@PathVariable Long id) {
        throw new EntityNotFoundException("Test entity", id);
    }
    
    @GetMapping("/test/generic-error")
    public String throwGenericException() {
        throw new RuntimeException("Test generic error");
    }
}
