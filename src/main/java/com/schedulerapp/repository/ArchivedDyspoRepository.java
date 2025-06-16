package com.schedulerapp.repository;

import com.schedulerapp.model.ArchivedDyspo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivedDyspoRepository extends JpaRepository<ArchivedDyspo, Long> {
}
