package com.kleberandrade.committeetimetable.utils;

import jakarta.persistence.*;
import java.util.*;

/** Legacy package retained for source compatibility. No connection is opened until requested. */
public final class EntityManagerUtil {
    public static final String PERSISTENCE_UNIT_NAME = "timetablingPU";
    private static EntityManagerFactory factory;
    private EntityManagerUtil() {}
    public static EntityManagerFactory createFactory(Map<String,String> environment) {
        var properties = new HashMap<String,Object>();
        properties.put("jakarta.persistence.jdbc.url", required(environment,"COMMITTEE_DB_URL"));
        properties.put("jakarta.persistence.jdbc.user", required(environment,"COMMITTEE_DB_USER"));
        properties.put("jakarta.persistence.jdbc.password", required(environment,"COMMITTEE_DB_PASSWORD"));
        return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME,properties);
    }
    private static String required(Map<String,String> environment, String key) {
        String value = environment.get(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Set " + key + " before using persistence");
        return value;
    }
    public static synchronized EntityManager getEntityManager() {
        if (needsFactory()) factory = createFactory(System.getenv());
        return factory.createEntityManager();
    }
    private static boolean needsFactory() { return factory == null || !factory.isOpen(); }
    public static synchronized void close() {
        if (factory != null) factory.close();
        factory = null;
    }
}
