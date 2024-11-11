//package com.example.clubreview.initializer;
//
//import com.example.clubreview.entity.User;
//import com.example.clubreview.repository.UserRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//@Configuration
//public class DatabaseInitializer {
//
//    private final UserRepository userRepository;
//    private final BCryptPasswordEncoder passwordEncoder;
//
//    public DatabaseInitializer(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Bean
//    CommandLineRunner initDatabase() {
//        return args -> {
//            userRepository.save(new User("testuser1", passwordEncoder.encode("password1"), User.Role.USER));
//            userRepository.save(new User("testuser2", passwordEncoder.encode("password2"), User.Role.USER));
//            userRepository.save(new User("testuser3", passwordEncoder.encode("password3"), User.Role.USER));
//            userRepository.save(new User("testuser4", passwordEncoder.encode("password4"), User.Role.USER));
//            userRepository.save(new User("testuser5", passwordEncoder.encode("password5"), User.Role.USER));
//        };
//    }
//}