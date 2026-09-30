package com.kurz.moduletests.notification;

/** Published by the notification module once every subscriber was e-mailed. */
public record NotificationSent(String title, int recipients) {
}
