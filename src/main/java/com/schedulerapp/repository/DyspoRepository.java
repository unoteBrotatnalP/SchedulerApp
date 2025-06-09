package com.schedulerapp.repository;

import com.schedulerapp.model.Dyspo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DyspoRepository extends JpaRepository<Dyspo, Long> {
}
