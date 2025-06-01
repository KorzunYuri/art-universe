package yurykorzun.art.universe.common.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@AllArgsConstructor
public class ResponseWrapper<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ResponseEntity<ResponseWrapper<T>> success(T data) {
        return ResponseEntity.ok(new ResponseWrapper<>(true, null, data));
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> failure(String message) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ResponseWrapper<>(false, message, null));
    }
}
