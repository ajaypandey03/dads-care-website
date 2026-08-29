package com.dadscare.backend.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSiteAccessRepository extends JpaRepository<UserSiteAccess, Long> {

    List<UserSiteAccess> findAllByUserId(Long userId);

    Optional<UserSiteAccess> findByUserIdAndSiteId(Long userId, Long siteId);

    void deleteAllByUserId(Long userId);
}
