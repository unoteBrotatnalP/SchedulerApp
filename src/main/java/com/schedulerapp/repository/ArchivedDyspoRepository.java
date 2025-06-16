package com.schedulerapp.repository;

import com.schedulerapp.model.ArchivedDyspo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ArchivedDyspoRepository extends JpaRepository<ArchivedDyspo, Long> {

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM ArchivedDyspo a WHERE YEAR(a.date) = :year AND MONTH(a.date) = :month")
    boolean existsByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
