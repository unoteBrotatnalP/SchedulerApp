package com.schedulerapp.repository;

import com.schedulerapp.model.User;
import com.schedulerapp.model.UserDyspoDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDyspoDetailsRepository extends JpaRepository<UserDyspoDetails, Long> {
    Optional<UserDyspoDetails> findByUser(User user);
}
