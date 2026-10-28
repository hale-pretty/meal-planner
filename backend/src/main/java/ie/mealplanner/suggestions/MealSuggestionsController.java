package ie.mealplanner.suggestions;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MealSuggestionsController {
    private final MealSuggestionsService service;
    public MealSuggestionsController(MealSuggestionsService service) { this.service = service; }
    @PostMapping(value = "/api/v1/meal-suggestions", consumes = "application/json", produces = "application/json")
    public MealSuggestionsResponse suggest(@RequestBody MealSuggestionsRequest request) { return service.suggest(request); }
}
