package com.workcollaboration.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.workcollaboration.support.Locators;
import com.workcollaboration.support.TestConfig;

class LoginTest extends BaseAppiumTest {

    @Test
    void validCredentialsOpenHomeScreen() {
        submitLogin(TestConfig.testEmail(), TestConfig.testPassword());

        assertEquals("Contacts", waitForId(Locators.CONTACTS_TAB_LABEL_ID).getText());
    }
}
