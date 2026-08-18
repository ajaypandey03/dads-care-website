package com.dadscare.backend.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    /** Login looks up by email alone (email is globally unique) — org is derived from the result, not supplied. */
    Optional<User> findByEmail(String email);

    List<User> findAllByOrganizationId(Long organizationId);

    Optional<User> findByIdAndOrganizationId(Long id, Long organizationId);
}
