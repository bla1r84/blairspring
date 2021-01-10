package com.blair.blairspring.repositories.userschema;

import com.blair.blairspring.model.userschema.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    @Modifying
    @Transactional
    Long deleteByUsername(String username);

}
