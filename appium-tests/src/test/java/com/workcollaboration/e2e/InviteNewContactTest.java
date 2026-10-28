package com.workcollaboration.e2e;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.workcollaboration.support.Locators;
import com.workcollaboration.support.TestConfig;

import io.appium.java_client.AppiumBy;

/** Adding an e-mail without an account prompts to send an invite; accepting closes the dialog. */
class InviteNewContactTest extends BaseAppiumTest {

    @Test
    void unknownEmailPromptsForInvite() {
        login();
        waitForClickable(AppiumBy.xpath(Locators.ADD_CONTACT_BUTTON_XPATH)).click();

        type(Locators.NEW_CONTACT_EMAIL_ID, TestConfig.newContactEmail());
        type(Locators.NEW_CONTACT_NAME_ID, TestConfig.newContactName());
        tapId(Locators.ADD_CONTACT_SUBMIT_ID);

        tapId(Locators.DIALOG_POSITIVE_BUTTON);

        wait.until(d -> d.findElements(AppiumBy.id(Locators.DIALOG_POSITIVE_BUTTON)).isEmpty());
        assertFalse(isPresent(Locators.DIALOG_POSITIVE_BUTTON), "Invite dialog still open");
    }
}
