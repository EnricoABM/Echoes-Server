package com.n0hana.echoes_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import com.n0hana.echoes_server.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    UserDetails findUserByEmail(String email);
}
