package com.workcollaboration.offline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.workcollaboration.support.TestConfig;

class TestConfigTest {

    @AfterEach
    void clearOverrides() {
        System.clearProperty("appium.url");
        System.clearProperty("wait.seconds");
    }

    @Test
    void defaultsAreUsedWhenNothingIsConfigured() {
        assumeTrue(System.getenv("APPIUM_URL") == null, "APPIUM_URL is set in this environment");

        assertEquals("http://127.0.0.1:4723", TestConfig.appiumUrl());
        assertTrue(TestConfig.appPath().isAbsolute());
        assertTrue(TestConfig.appPath().endsWith("apk/WorkCollaboration.apk")
                || TestConfig.appPath().endsWith("apk/debug/app-debug.apk"), TestConfig.appPath().toString());
    }

    @Test
    void systemPropertyOverridesDefault() {
        System.setProperty("appium.url", "http://example.test:4999");
        System.setProperty("wait.seconds", "5");

        assertEquals("http://example.test:4999", TestConfig.appiumUrl());
        assertEquals(Duration.ofSeconds(5), TestConfig.waitTimeout());
    }

    @Test
    void blankPropertyFallsBackToDefault() {
        assumeTrue(System.getenv("APPIUM_URL") == null, "APPIUM_URL is set in this environment");
        System.setProperty("appium.url", "   ");

        assertEquals("http://127.0.0.1:4723", TestConfig.appiumUrl());
    }
}
