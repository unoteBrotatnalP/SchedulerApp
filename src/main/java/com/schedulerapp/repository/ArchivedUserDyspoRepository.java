package com.schedulerapp.repository;

import com.schedulerapp.model.ArchivedUserDyspo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivedUserDyspoRepository extends JpaRepository<ArchivedUserDyspo, Long> {
}
