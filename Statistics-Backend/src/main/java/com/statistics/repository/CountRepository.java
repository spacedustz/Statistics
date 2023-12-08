package com.statistics.repository;

import com.statistics.entity.Count;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountRepository extends JpaRepository<Count, String> {
}
