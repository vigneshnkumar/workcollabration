package com.workcollaboration.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.workcollaboration.support.Locators;

import io.appium.java_client.AppiumBy;

class DeleteContactTest extends BaseAppiumTest {

    @Test
    void deleteFirstContactRemovesIt() {
        login();
        waitForId(Locators.CONTACT_ROW_ID);
        String name = driver.findElement(AppiumBy.id(Locators.CONTACT_NAME_ID)).getText();

        tapId(Locators.CONTACT_ROW_ID);       // expands the row to reveal the delete icon
        tapId(Locators.CONTACT_DELETE_ID);

        assertTrue(waitForTextGone(Locators.CONTACT_NAME_ID, name),
                "Contact still listed after delete: " + name);
    }
}
