package com.example.richard.vybe.Model;

public enum EndPoints {

    RECENTLY_PLAYED("https://api.spotify.com/v1/me/player/recently-played"),
    TRACKS("https://api.spotify.com/v1/me/tracks"),
    PLAYLIST("https://api.spotify.com/v1/playlists/%s/tracks"),
    PLAYLISTME("https://api.spotify.com/v1/me/playlists"),
    FEATUREDPLAYLISTS("https://api.spotify.com/v1/browse/featured-playlists"),
    USER("https://api.spotify.com/v1/me"),
    ;

    private final String endpoint;

    EndPoints(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString(){
        return  endpoint;
    }
}
