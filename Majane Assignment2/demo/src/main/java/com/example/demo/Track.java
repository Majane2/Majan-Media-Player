package com.example.demo;

import javafx.scene.image.Image;

public class Track {

    private final String title;
    private final String artist;
    private final String album;
    private final String filePath;
    private final String artworkPath;
    private String duration;

    // Video snapshot thumbnail (or null if not captured yet)
    private Image coverImage;

    // True once we've tried to capture a cover and either succeeded or failed
    private boolean coverAttempted = false;

    public Track(String title,
                 String artist,
                 String album,
                 String filePath,
                 String artworkPath,
                 String duration) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.filePath = filePath;
        this.artworkPath = artworkPath;
        this.duration = duration;
    }

    public String getTitle()       { return title; }
    public String getArtist()      { return artist; }
    public String getAlbum()       { return album; }
    public String getFilePath()    { return filePath; }
    public String getArtworkPath() { return artworkPath; }
    public String getDuration()    { return duration; }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Image getCoverImage()             { return coverImage; }
    public void setCoverImage(Image image)   { this.coverImage = image; }

    public boolean isCoverAttempted()        { return coverAttempted; }
    public void setCoverAttempted(boolean b) { this.coverAttempted = b; }

    public String getFileType() {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".mp3")) return "MP3";
        if (lower.endsWith(".wav")) return "WAV";
        if (lower.endsWith(".m4a")) return "M4A";
        if (lower.endsWith(".aac")) return "AAC";
        if (lower.endsWith(".mp4")) return "MP4";
        if (lower.endsWith(".m4v")) return "M4V";
        if (lower.endsWith(".mov")) return "MOV";
        return "MEDIA";
    }

    public boolean isVideo() {
        String lower = filePath.toLowerCase();
        return lower.endsWith(".mp4")
                || lower.endsWith(".m4v")
                || lower.endsWith(".mov");
    }

    @Override
    public String toString() {
        return title;
    }
}