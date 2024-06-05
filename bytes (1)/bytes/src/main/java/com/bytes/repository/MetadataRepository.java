package com.bytes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bytes.model.Metadata;

@Repository
public interface MetadataRepository extends JpaRepository<Metadata, Long> {
	Metadata findByEmail(String email);

	List<Metadata> findByBytesId(String bytesId);

}
