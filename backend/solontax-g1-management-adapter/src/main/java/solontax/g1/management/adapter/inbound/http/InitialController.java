package solontax.g1.management.adapter.inbound.http;


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
