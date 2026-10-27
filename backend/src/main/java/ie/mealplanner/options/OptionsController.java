package ie.mealplanner.options;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OptionsController {
    private final OptionsService service;

    public OptionsController(OptionsService service) { this.service = service; }

    @GetMapping(value = "/api/v1/options", produces = "application/json")
    public OptionsResponse options() { return service.options(); }
}
