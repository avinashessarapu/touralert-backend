package com.touralert.config;

import com.touralert.model.User;
import com.touralert.model.Trip;
import com.touralert.model.Incident;
import com.touralert.repository.UserRepository;
import com.touralert.repository.TripRepository;
import com.touralert.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

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

            // 1. Seed regular traveler account
            User traveler = new User();
            traveler.setUsername("avinash_travels");
            traveler.setEmail("avinash@gmail.com");
            traveler.setPassword(passwordEncoder.encode("travelpass123")); // Securely Hashed
            traveler.setRole("USER");
            userRepository.save(traveler);

            // 2. Seed administrative account
            User admin = new User();
            admin.setUsername("system_admin");
            admin.setEmail("admin@touralert.com");
            admin.setPassword(passwordEncoder.encode("adminsecure456")); // Securely Hashed
            admin.setRole("ADMIN");
            userRepository.save(admin);

            // 3. Seed a mock planned trip for our traveler heading to Araku
            Trip trip = new Trip();
            trip.setDestination("Araku Valley");
            trip.setStartLocation("Visakhapatnam");
            trip.setStartDate(LocalDate.now().plusDays(5));
            trip.setEndDate(LocalDate.now().plusDays(8));
            trip.setStatus("PLANNED");
            trip.setTraveler(traveler);
            tripRepository.save(trip);

            // 4. Seed an active reported landslide near Araku Valley road
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