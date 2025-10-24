package com.metabots.festora;

public class Event {
    public final String id;
    public final String title;
    public final String dateTime;
    public final String location;
    public final String imageUrl;
    public final String source;
    public final String bookingUrl;

    public Event(String id, String title, String dateTime, String location, String imageUrl) {
        this(id, title, dateTime, location, imageUrl, null, null);
    }

    public Event(String id, String title, String dateTime, String location, String imageUrl, String source, String bookingUrl) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.location = location;
        this.imageUrl = imageUrl;
        this.source = source;
        this.bookingUrl = bookingUrl;
    }
}
