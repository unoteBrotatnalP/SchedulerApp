package com.schedulerapp.repository;

import com.schedulerapp.model.Dyspo;
import com.schedulerapp.model.User;
import com.schedulerapp.model.UserDyspo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDyspoRepository extends JpaRepository<UserDyspo, Long> {
    Optional<UserDyspo> findByDyspoAndUser(Dyspo dyspo, User user);
}
