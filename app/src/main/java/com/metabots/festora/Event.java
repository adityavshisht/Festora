package com.metabots.festora;

public class Event {
    public final String id;
    public final String title;
    public final String dateTime;
    public final String location;
    public final String imageUrl; // optional (for later: Glide/picasso)

    public Event(String id, String title, String dateTime, String location, String imageUrl) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.location = location;
        this.imageUrl = imageUrl;
    }
}
