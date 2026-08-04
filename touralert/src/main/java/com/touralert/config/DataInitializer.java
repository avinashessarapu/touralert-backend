package com.touralert.config;

import com.touralert.model.Incident;
import com.touralert.repository.IncidentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final IncidentRepository incidentRepository;

    public DataInitializer(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Override
    public void run(String... args) {
        if (incidentRepository.count() == 0) {
            incidentRepository.saveAll(List.of(
                    buildIncident("LANDSLIDE", "Rockfall on the Araku ghat road has reduced visibility and blocked a lane.", "Araku Ghat Road", 18.2850, 82.9110),
                    buildIncident("FLOODING", "Waterlogging near the river crossing is affecting traffic flow for inbound vehicles.", "River Crossing", 17.6868, 83.2185),
                    buildIncident("ACCIDENT", "A collision has caused a temporary closure near the main bypass junction.", "Main Bypass Junction", 18.3160, 82.9705)
            ));
        }
    }

    private Incident buildIncident(String type, String description, String routeOrLocation, double latitude, double longitude) {
        Incident incident = new Incident();
        incident.setType(type);
        incident.setDescription(description);
        incident.setRouteOrLocation(routeOrLocation);
        incident.setLatitude(latitude);
        incident.setLongitude(longitude);
        incident.setStatus("OPEN");
        incident.setReportedAt(LocalDateTime.now());
        return incident;
    }
}
