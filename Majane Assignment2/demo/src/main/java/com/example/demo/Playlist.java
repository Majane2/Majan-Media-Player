package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Playlist {

    private final String name;

    private final ObservableList<Track> tracks =
            FXCollections.observableArrayList();

    public Playlist(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ObservableList<Track> getTracks() {
        return tracks;
    }

    public void addTrack(Track track) {
        tracks.add(track);
    }

    public void removeTrack(Track track) {
        tracks.remove(track);
    }

    public int size() {
        return tracks.size();
    }

    @Override
    public String toString() {
        return name;
    }
}