package com.claude.learn.repository;

import com.claude.learn.domain.UserQuota;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserQuotaRepository extends JpaRepository<UserQuota, String> {}