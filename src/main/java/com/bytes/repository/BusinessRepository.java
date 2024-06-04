package com.bytes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bytes.model.Business;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    List<Business> findByEmail(String email);


}
