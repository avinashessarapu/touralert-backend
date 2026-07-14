package com.touralert.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class ConfigController {

    @Value("${maps.api.key:}")
    private String mapsApiKey;

    @GetMapping("/api/config")
    public Map<String, String> getConfig() {
        return Map.of("mapsApiKey", mapsApiKey == null ? "" : mapsApiKey);
    }
}
