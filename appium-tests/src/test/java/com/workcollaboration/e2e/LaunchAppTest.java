package com.workcollaboration.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.workcollaboration.support.Locators;

class LaunchAppTest extends BaseAppiumTest {

    @Test
    void appLaunchesToLoginScreen() {
        // The splash screen hands over to the login page when no user is signed in.
        assertTrue(waitForId(Locators.EMAIL_ID).isDisplayed());
        assertEquals(Locators.APP_PACKAGE, driver.getCurrentPackage());
    }
}
