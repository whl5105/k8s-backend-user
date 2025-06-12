package com.welab.k8s_backend_user.domain.repository;

import com.welab.k8s_backend_user.domain.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SiteUserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByUserId(String userId);
}
