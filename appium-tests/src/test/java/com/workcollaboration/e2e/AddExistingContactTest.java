package com.workcollaboration.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.workcollaboration.support.Locators;
import com.workcollaboration.support.TestConfig;

import io.appium.java_client.AppiumBy;

/** Adds a user who already has an account; the app returns to the contact list. */
class AddExistingContactTest extends BaseAppiumTest {

    @Test
    void addExistingUserAsContact() {
        login();
        waitForClickable(AppiumBy.xpath(Locators.ADD_CONTACT_BUTTON_XPATH)).click();

        type(Locators.NEW_CONTACT_EMAIL_ID, TestConfig.existingContactEmail());
        type(Locators.NEW_CONTACT_NAME_ID, TestConfig.existingContactName());
        tapId(Locators.ADD_CONTACT_SUBMIT_ID);

        String name = TestConfig.existingContactName();
        assertTrue(waitForText(Locators.CONTACT_NAME_ID, name),
                "Contact not shown in the contact list: " + name);
    }
}
