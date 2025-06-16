package com.schedulerapp.repository;

import com.schedulerapp.model.ArchivedUserDyspoDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivedUserDyspoDetailsRepository extends JpaRepository<ArchivedUserDyspoDetails, Long> {
}
