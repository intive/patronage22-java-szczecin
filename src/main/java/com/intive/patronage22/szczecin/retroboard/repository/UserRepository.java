package com.intive.patronage22.szczecin.retroboard.repository;

import com.intive.patronage22.szczecin.retroboard.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

    Optional<User> findUserByEmail(final String email);

    List<User> findAllByEmailIn(List<String> emails);

    @Query("SELECT u.email FROM User u WHERE u.email LIKE %:email%")
    List<String> searchByEmailLike(final String email);
}
