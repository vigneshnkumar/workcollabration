package com.workcollaboration.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.workcollaboration.support.Locators;
import com.workcollaboration.support.TestConfig;

class MyAccountTest extends BaseAppiumTest {

    @Test
    void accountPageShowsSignedInEmail() {
        login();
        tapId(Locators.MY_ACCOUNT_TAB_ID);

        assertEquals(TestConfig.testEmail(), waitForId(Locators.ACCOUNT_EMAIL_ID).getText());
    }
}
