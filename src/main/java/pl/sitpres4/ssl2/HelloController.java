package pl.sitpres4.ssl2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        String name = "World";
        return String.format("Hello %s!", name);
    }
}
