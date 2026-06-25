package com.touralert.config;

import com.touralert.model.User;
import com.touralert.model.Trip;
import com.touralert.model.Incident;
import com.touralert.repository.UserRepository;
import com.touralert.repository.TripRepository;
import com.touralert.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Override
    public void run(String... args) throws Exception {
        // Prevent duplicate seed inserts if restarting active storage
        if (userRepository.count() == 0) {
            System.out.println("====== SEEDING TEST DATABASE CHANNELS ======");

            // 1. Seed regular traveler and admin accounts
            User traveler = new User();
            traveler.setUsername("avinash_travels");
            traveler.setEmail("avinash@gmail.com");
            traveler.setPassword("travelpass123");
            traveler.setRole("USER");
            userRepository.save(traveler);

            User admin = new User();
            admin.setUsername("system_admin");
            admin.setEmail("admin@touralert.com");
            admin.setPassword("adminsecure456");
            admin.setRole("ADMIN");
            userRepository.save(admin);

            // 2. Seed a mock planned trip for our traveler heading to Araku
            Trip trip = new Trip();
            trip.setDestination("Araku Valley");
            trip.setStartLocation("Visakhapatnam");
            trip.setStartDate(LocalDate.now().plusDays(5));
            trip.setEndDate(LocalDate.now().plusDays(8));
            trip.setStatus("PLANNED");
            trip.setTraveler(traveler);
            tripRepository.save(trip);

            // 3. Seed an active reported landslide near Araku Valley road
            Incident landslide = new Incident();
            landslide.setType("LANDSLIDE");
            landslide.setDescription("Heavy rocks blocking major route lanes on Ghat road section.");
            landslide.setRouteOrLocation("Araku Ghat Road");
            landslide.setStatus("PENDING");
            landslide.setReporter(traveler);
            landslide.setReportedAt(java.time.LocalDateTime.now());
            incidentRepository.save(landslide);

            System.out.println("====== DATABASE SEEDING LOGS COMPLETED SUCCESSFULLY ======");
        }
    }
}