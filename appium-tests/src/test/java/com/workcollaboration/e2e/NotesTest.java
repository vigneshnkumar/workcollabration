package com.workcollaboration.e2e;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.workcollaboration.support.Locators;
import com.workcollaboration.support.TestConfig;

import io.appium.java_client.AppiumBy;

/** Saves the conversation's shared note via the Notes dialog. */
class NotesTest extends BaseAppiumTest {

    @Test
    void saveSharedNote() {
        login();
        openConversation(TestConfig.chatConversation());

        tapId(Locators.NOTES_BUTTON_ID);
        type(Locators.NOTE_INPUT_ID, TestConfig.noteText());
        tapId(Locators.DIALOG_OK_ID);

        wait.until(d -> d.findElements(AppiumBy.id(Locators.NOTE_INPUT_ID)).isEmpty());
        assertFalse(isPresent(Locators.NOTE_INPUT_ID), "Notes dialog still open after saving");
    }
}
