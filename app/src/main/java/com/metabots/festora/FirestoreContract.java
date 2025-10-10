package com.metabots.festora.data;

public final class FirestoreContract {
    private FirestoreContract() {}

    public static final class Collections {
        public static final String EVENTS = "events";
    }

    public static final class EventFields {
        public static final String TITLE      = "title";
        public static final String DATE_TEXT  = "dateText";
        public static final String LOCATION   = "location";
        public static final String IMAGE_URL  = "imageUrl";
        public static final String CATEGORY   = "category";
        public static final String DESCRIPTION= "description";
        public static final String HOST_UID   = "hostUid";
        public static final String HOST_EMAIL = "hostEmail";
        public static final String CREATED_AT = "createdAt";
    }
}
