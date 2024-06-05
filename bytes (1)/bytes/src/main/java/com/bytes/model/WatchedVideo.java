package com.bytes.model;

import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "watched_videos")
public class WatchedVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "video_id", referencedColumnName = "id")
    private Bytes video;

    @Column(name = "watched_date_time")
    private LocalDateTime watchedDateTime;

    // Constructors, getters, and setters

    public WatchedVideo() {
    }

    public WatchedVideo(User user, Bytes video, LocalDateTime watchedDateTime) {
        this.user = user;
        this.video = video;
        this.watchedDateTime = watchedDateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Bytes getVideo() {
        return video;
    }

    public void setVideo(Bytes video) {
        this.video = video;
    }

    public LocalDateTime getWatchedDateTime() {
        return watchedDateTime;
    }

    public void setWatchedDateTime(LocalDateTime watchedDateTime) {
        this.watchedDateTime = watchedDateTime;
    }
}
