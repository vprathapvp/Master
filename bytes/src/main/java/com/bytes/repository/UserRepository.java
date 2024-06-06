package com.bytes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bytes.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	User findByEmail(String email);

	boolean existsByEmail(String email);
	
	@Query("SELECT u.profileImage FROM User u WHERE u.id = :id")
	byte[] findProfileImageById(Long id);
//	 Optional<User> findByEmail(String email);
	
}