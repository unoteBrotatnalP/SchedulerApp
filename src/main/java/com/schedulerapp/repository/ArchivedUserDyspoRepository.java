package com.schedulerapp.repository;

import com.schedulerapp.model.ArchivedUserDyspo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivedUserDyspoRepository extends JpaRepository<ArchivedUserDyspo, Long> {

    @Modifying
    @Transactional
    @Query(value = "DELETE aud FROM archived_user_dyspo aud JOIN archived_dyspo ad ON aud.archived_dyspo_id = ad.id WHERE YEAR(ad.date) = :year AND MONTH(ad.date) = :month", nativeQuery = true)
    void deleteByYearAndMonth(@Param("year") int year, @Param("month") int month);

}
