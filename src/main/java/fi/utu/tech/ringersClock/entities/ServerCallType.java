package fi.utu.tech.ringersClock.entities;

public enum ServerCallType {
    ALARM_TIME_CANCELLED,
    ALARM_TIME_RESPONSE,
    STATUS_UPDATE,
    ALARM_USER,
    CONFIRM_ALARM,
    UPDATE_EXISTING_GROUPS
}