package com.user_service;

import com.common_service.enums.AuthProvider;
import com.common_service.enums.UserRole;
import com.common_service.enums.UserStatus;
import com.common_service.model.entity.Permission;
import com.common_service.model.entity.Role;
import com.common_service.model.entity.User;
import com.common_service.repository.PermissionRepository;
import com.common_service.repository.RoleRepository;
import com.common_service.repository.UserRepository;
import com.user_service.utils.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private Argon2PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) {
        // First, ensure permissions exist
      //  createPermissionsIfNotExist();

        // Then create roles with existing permissions
       // createRolesIfNotExist();

        // Finally create users
        createUsersIfNotExist();
    }

    private void createPermissionsIfNotExist() {
        if (permissionRepository.findByName(Permissions.USER_PROFILE_SHOW).isEmpty()) {
            permissionRepository.save(new Permission(Permissions.USER_PROFILE_SHOW));
        }
        if (permissionRepository.findByName(Permissions.USER_PROFILE_UPDATE).isEmpty()) {
            permissionRepository.save(new Permission(Permissions.USER_PROFILE_UPDATE));
        }
        if (permissionRepository.findByName(Permissions.USER_LIST).isEmpty()) {
            permissionRepository.save(new Permission(Permissions.USER_LIST));
        }
    }

    private void createRolesIfNotExist() {
        // Create USER role if it doesn't exist
        if (roleRepository.findByName(UserRole.USER.name()).isEmpty()) {
            Role userRole = new Role(UserRole.USER.name());
            permissionRepository.findByName(Permissions.USER_PROFILE_SHOW)
                    .ifPresent(userRole.getPermissions()::add);
            permissionRepository.findByName(Permissions.USER_PROFILE_UPDATE)
                    .ifPresent(userRole.getPermissions()::add);
            roleRepository.save(userRole);
        }

        // Create ADMIN role if it doesn't exist
        if (roleRepository.findByName(UserRole.ADMIN.name()).isEmpty()) {
            Role adminRole = new Role(UserRole.ADMIN.name());
            permissionRepository.findByName(Permissions.USER_LIST)
                    .ifPresent(adminRole.getPermissions()::add);
            roleRepository.save(adminRole);
        }
    }

    private void createUsersIfNotExist() {
        if (userRepository.findByUsername("digital_bank").isEmpty()) {
            String hashedPassword = passwordEncoder.encode("Az12345678");

            Role adminRole = roleRepository.findByName(UserRole.ADMIN.name())
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            User user = User.builder()
                    .username("digital_bank")
                    .email("digital_bank@gmail.com")
                    .passwordHash(hashedPassword)
                    .status(UserStatus.ACTIVE)
                    .provider(AuthProvider.LOCAL)
                    .mfaEnabled(false)
                    .roles(new HashSet<>())
                    .build();
            user.getRoles().add(adminRole);
            userRepository.save(user);
            System.out.println("User created: " + user.getUsername());
        }
    }
}
