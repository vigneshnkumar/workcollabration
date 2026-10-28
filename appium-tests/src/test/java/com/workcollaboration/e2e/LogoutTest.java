package com.workcollaboration.e2e;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.workcollaboration.support.Locators;

class LogoutTest extends BaseAppiumTest {

    @Test
    void logoutReturnsToLoginScreen() {
        login();
        tapId(Locators.MY_ACCOUNT_TAB_ICON_ID);
        tapId(Locators.LOGOUT_ID);

        assertTrue(waitForId(Locators.SIGN_IN_ID).isDisplayed());
        assertFalse(isPresent(Locators.CONTACTS_TAB_LABEL_ID));
    }
}
