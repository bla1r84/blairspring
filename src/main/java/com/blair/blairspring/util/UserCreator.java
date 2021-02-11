package com.blair.blairspring.util;

import com.blair.blairspring.model.ibatisschema.Team;
import com.blair.blairspring.model.userschema.Role;
import com.blair.blairspring.model.userschema.User;
import com.blair.blairspring.repositories.userschema.RoleRepository;
import com.blair.blairspring.services.UserService;
import com.blair.blairspring.services.implementations.jpa.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

@Profile("user-creator")
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserCreator {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final TeamService teamService;

    public void createUsers() {
        log.info("isActualTransactionActive: {}", TransactionSynchronizationManager.isActualTransactionActive());
        Role userRole = new Role();
        userRole.setRoleName("ROLE_USER");
        Role testerRole = new Role();
        testerRole.setRoleName("ROLE_TESTER");
        Role adminRole = new Role();
        adminRole.setRoleName("ROLE_ADMIN");

        List.of(userRole, testerRole, adminRole).forEach(roleRepository::save);

        /*Team newTeam = new Team();
        newTeam.setName("PAOK");
        newTeam.setGreekName("ΠΑΟΚ");
        teamService.create(newTeam);*/

        User john = new User();
        john.setUsername("john");
        john.setPassword("doe");
        john.setRoles(Set.of(userRole));

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("admin");
        admin.setRoles(Set.of(userRole, adminRole));

        User jane = new User();
        jane.setUsername("jane");
        jane.setPassword("doe");
        jane.setRoles(Set.of(userRole, testerRole));

        List.of(john, admin, jane).forEach(user -> userService.registerUser(user));

    }

}
