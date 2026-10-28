package com.workcollaboration.e2e;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.workcollaboration.support.Locators;
import com.workcollaboration.support.TestConfig;

import io.appium.java_client.AppiumBy;

/**
 * Sets a reminder for the conversation. The original script opened the Notes dialog by
 * mistake; reminders live behind the "camera" icon and the custom.xml dialog.
 */
class ReminderTest extends BaseAppiumTest {

    @Test
    void setReminderForTomorrow() {
        login();
        openConversation(TestConfig.chatConversation());

        LocalDateTime when = LocalDateTime.now().plusDays(1);
        tapId(Locators.REMINDER_BUTTON_ID);
        type(Locators.REMINDER_NAME_ID, TestConfig.reminderText());
        type(Locators.REMINDER_DAY_ID, String.valueOf(when.getDayOfMonth()));
        type(Locators.REMINDER_MONTH_ID, String.valueOf(when.getMonthValue()));
        type(Locators.REMINDER_YEAR_ID, String.valueOf(when.getYear()));
        type(Locators.REMINDER_HOUR_ID, String.valueOf(when.getHour()));
        type(Locators.REMINDER_MINUTE_ID, String.valueOf(when.getMinute()));
        tapId(Locators.DIALOG_OK_ID);

        wait.until(d -> d.findElements(AppiumBy.id(Locators.REMINDER_NAME_ID)).isEmpty());
        assertFalse(isPresent(Locators.REMINDER_NAME_ID), "Reminder dialog still open after saving");
    }
}
