package com.touralert;

import com.touralert.service.JourneyGuidanceService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JourneyGuidanceServiceTest {

    private final JourneyGuidanceService service = new JourneyGuidanceService();

    @Test
    void shouldCreateHazardSpecificSuggestion() {
        String suggestion = service.buildSuggestion("LANDSLIDE", "Araku Valley", "Visakhapatnam");

        assertTrue(suggestion.contains("Araku Valley"));
        assertTrue(suggestion.toLowerCase().contains("alternate"));
    }
}
