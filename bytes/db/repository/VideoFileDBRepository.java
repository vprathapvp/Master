package com.bezkoder.spring.files.upload.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bezkoder.spring.files.upload.db.model.VideoFileDB;

@Repository
public interface VideoFileDBRepository extends JpaRepository<VideoFileDB, String> {

}
