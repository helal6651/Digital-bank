package com.user_service;

import com.user_service.model.entity.Permission;
import com.user_service.model.entity.Role;
import com.user_service.repository.PermissionRepository;
import com.user_service.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;



    @Override
    public void run(String... args) {
        Permission updateProfile = permissionRepository.save(new Permission("USER_UPDATE_PROFILE_PERMISSION"));
        Role userRole = new Role("ADMIN");
        userRole.getPermissions().add(updateProfile);
        roleRepository.save(userRole);


    }
}
