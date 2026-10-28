package com.workcollaboration.e2e;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.workcollaboration.support.Locators;
import com.workcollaboration.support.TestConfig;

class LoginInvalidTest extends BaseAppiumTest {

    @Test
    void invalidCredentialsStayOnLoginScreen() {
        submitLogin(TestConfig.invalidEmail(), TestConfig.invalidPassword());

        // The app shows a toast and keeps the login form; the home tab bar must never appear.
        assertTrue(waitForId(Locators.SIGN_IN_ID).isDisplayed());
        assertFalse(isPresent(Locators.CONTACTS_TAB_LABEL_ID), "Home screen shown for invalid credentials");
    }
}
