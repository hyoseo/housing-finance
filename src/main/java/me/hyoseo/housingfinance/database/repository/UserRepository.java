package me.hyoseo.housingfinance.database.repository;

import me.hyoseo.housingfinance.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
