package com.metabots.festora.model;

import java.util.HashMap;
import java.util.Map;

import static com.metabots.festora.data.FirestoreContract.EventFields.*;

public class EventDoc {
    public String id;          // Firestore doc id (not stored inside doc by default)
    public String title;
    public String dateText;
    public String location;
    public String imageUrl;
    public String category;
    public String description;
    public String hostUid;
    public String hostEmail;

    public EventDoc() {} // needed for Firestore toObject()

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put(TITLE, title);
        m.put(DATE_TEXT, dateText);
        m.put(LOCATION, location);
        m.put(IMAGE_URL, imageUrl);
        m.put(CATEGORY, category);
        m.put(DESCRIPTION, description);
        m.put(HOST_UID, hostUid);
        m.put(HOST_EMAIL, hostEmail);
        return m;
    }
}
