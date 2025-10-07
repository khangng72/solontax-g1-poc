package solontax.g1.hexmanagement.infrastructure.adapter.in.rest;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InitialController {
    @GetMapping
    public ResponseEntity<String> getInitialMessage() {
        return ResponseEntity.ok("Welcome to Hexagonal Solon Tax G1");
    }
}
