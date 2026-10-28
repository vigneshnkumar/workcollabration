package com.workcollaboration.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Central test configuration.
 *
 * <p>Every value is resolved in this order: JVM system property ({@code -Dappium.url=...}),
 * environment variable (upper-cased, dots replaced by underscores, e.g. {@code APPIUM_URL}),
 * then the built-in default. Nothing is ever read from standard input, so the suite can run
 * unattended in CI.
 */
public final class TestConfig {

    private TestConfig() {
    }

    // --- Appium / device ---------------------------------------------------------------

    public static String appiumUrl() {
        return get("appium.url", "http://127.0.0.1:4723");
    }

    /**
     * Defaults to a fresh Gradle debug build when one exists; otherwise falls back to the
     * prebuilt 2016 APK (targetSdk 21, which Android 14+ refuses to install).
     */
    public static Path appPath() {
        String fallback = Files.isRegularFile(moduleDir().resolve(GRADLE_DEBUG_APK))
                ? GRADLE_DEBUG_APK : PREBUILT_APK;
        Path path = Paths.get(get("app.path", fallback));
        return path.isAbsolute() ? path.normalize() : moduleDir().resolve(path).normalize();
    }

    static final String GRADLE_DEBUG_APK = "../app/build/outputs/apk/debug/app-debug.apk";
    static final String PREBUILT_APK = "../apk/WorkCollaboration.apk";

    public static String deviceName() {
        return get("device.name", "Android Emulator");
    }

    /** Optional; empty means "let Appium pick any connected device". */
    public static String platformVersion() {
        return get("platform.version", "");
    }

    public static Duration waitTimeout() {
        return Duration.ofSeconds(Long.parseLong(get("wait.seconds", "30")));
    }

    // --- Test data (previously typed interactively via Scanner(System.in)) --------------

    public static String testEmail() {
        return get("test.email", "test");
    }

    public static String testPassword() {
        return get("test.password", "test");
    }

    public static String invalidEmail() {
        return get("test.invalid.email", "aaaa");
    }

    public static String invalidPassword() {
        return get("test.invalid.password", "bbbb123");
    }

    /** E-mail of a user that already has an account (AddExistingContactTest). */
    public static String existingContactEmail() {
        return get("contact.existing.email", "existing.user@example.com");
    }

    public static String existingContactName() {
        return get("contact.existing.name", "Existing User");
    }

    /** E-mail of a user without an account, which triggers the invite dialog. */
    public static String newContactEmail() {
        return get("contact.new.email", "new.user." + System.currentTimeMillis() + "@example.com");
    }

    public static String newContactName() {
        return get("contact.new.name", "New User");
    }

    /** Title of the one-to-one conversation to open; empty means the first conversation. */
    public static String chatConversation() {
        return get("chat.conversation", "");
    }

    /** Title of the group conversation to open; empty means the first conversation. */
    public static String groupConversation() {
        return get("chat.group", "");
    }

    public static String chatMessage() {
        return get("chat.message", "Hello from Appium");
    }

    public static String noteText() {
        return get("note.text", "Shared note from Appium");
    }

    public static String reminderText() {
        return get("reminder.text", "Appium reminder");
    }

    // --- Paths -------------------------------------------------------------------------

    /** The appium-tests module directory (Surefire sets {@code basedir}). */
    public static Path moduleDir() {
        return Paths.get(System.getProperty("basedir", System.getProperty("user.dir")))
                .toAbsolutePath().normalize();
    }

    /** Layout XML directory of the Android app, used by the offline locator contract test. */
    public static Path layoutDir() {
        return moduleDir().resolve("../app/src/main/res/layout").normalize();
    }

    static String get(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        return isBlank(value) ? defaultValue : value.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
