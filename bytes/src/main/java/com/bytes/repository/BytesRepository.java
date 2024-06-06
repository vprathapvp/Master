package com.bytes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bytes.model.Bytes;
import com.bytes.model.User;

@Repository
public interface BytesRepository extends JpaRepository<Bytes, String> {
	Bytes findById(Long id);

	List<Bytes> findByUserEmail(String email);

	@Query("SELECT ud FROM User ud WHERE ud.email = ?1")
	User findUserDetailsByEmail(String email);
}
