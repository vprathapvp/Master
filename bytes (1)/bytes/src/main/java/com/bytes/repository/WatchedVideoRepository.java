package com.bytes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bytes.model.WatchedVideo;

@Repository
public interface WatchedVideoRepository extends JpaRepository<WatchedVideo, Long> {
    List<WatchedVideo> findByUserId(Long userId);
}