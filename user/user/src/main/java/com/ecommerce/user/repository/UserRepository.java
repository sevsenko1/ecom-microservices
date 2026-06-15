package com.ecommerce.user.repository;


import com.ecommerce.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository; // better way
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

}
