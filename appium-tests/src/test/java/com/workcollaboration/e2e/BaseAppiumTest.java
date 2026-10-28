package com.workcollaboration.e2e;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.workcollaboration.support.Locators;
import com.workcollaboration.support.TestConfig;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

/**
 * Starts a fresh app session per test and provides the shared login / navigation steps.
 *
 * <p>Tests are skipped (not failed) when the APK or an Appium server is not available, so
 * {@code mvn test} stays green on machines without an emulator.
 */
@Tag("e2e")
abstract class BaseAppiumTest {

    protected AndroidDriver driver;
    protected WebDriverWait wait;

    @BeforeEach
    void startSession() throws Exception {
        Path app = TestConfig.appPath();
        assumeTrue(Files.isRegularFile(app), "APK not found at " + app + " (set -Dapp.path)");
        assumeTrue(isServerReachable(TestConfig.appiumUrl()),
                "Appium server not reachable at " + TestConfig.appiumUrl()
                        + " - start it with `appium` (see README) or set -Dappium.url");

        UiAutomator2Options options = new UiAutomator2Options()
                .setDeviceName(TestConfig.deviceName())
                .setApp(app.toString())
                .setAutoGrantPermissions(true)
                .setNewCommandTimeout(Duration.ofSeconds(100));
        if (!TestConfig.platformVersion().isEmpty()) {
            options.setPlatformVersion(TestConfig.platformVersion());
        }

        driver = new AndroidDriver(URI.create(TestConfig.appiumUrl()).toURL(), options);
        wait = new WebDriverWait(driver, TestConfig.waitTimeout());
    }

    @AfterEach
    void stopSession() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --- helpers -----------------------------------------------------------------------

    protected WebElement waitForId(String id) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.id(id)));
    }

    protected WebElement waitForClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    protected void tapId(String id) {
        waitForClickable(AppiumBy.id(id)).click();
    }

    protected void type(String id, String text) {
        WebElement field = waitForId(id);
        field.clear();
        field.sendKeys(text);
    }

    /** Waits until some element with the given id shows {@code text}; times out otherwise. */
    protected boolean waitForText(String id, String text) {
        return wait.until(d -> d.findElements(AppiumBy.id(id)).stream()
                .anyMatch(e -> text.equals(e.getText())));
    }

    /** Waits until no element with the given id shows {@code text}; times out otherwise. */
    protected boolean waitForTextGone(String id, String text) {
        return wait.until(d -> d.findElements(AppiumBy.id(id)).stream()
                .noneMatch(e -> text.equals(e.getText())));
    }

    protected boolean isPresent(String id) {
        return !driver.findElements(AppiumBy.id(id)).isEmpty();
    }

    protected void submitLogin(String email, String password) {
        type(Locators.EMAIL_ID, email);
        type(Locators.PASSWORD_ID, password);
        tapId(Locators.SIGN_IN_ID);
    }

    /** Logs in with the configured account and waits for the home screen tab bar. */
    protected void login() {
        submitLogin(TestConfig.testEmail(), TestConfig.testPassword());
        waitForId(Locators.CONTACTS_TAB_LABEL_ID);
    }

    /** Opens the conversation with the given title, or the first one if the title is empty. */
    protected void openConversation(String title) {
        tapId(Locators.CHAT_TAB_ICON_ID);
        waitForId(Locators.CONVERSATION_LIST_ID);
        List<WebElement> titles = wait.until(
                ExpectedConditions.numberOfElementsToBeMoreThan(AppiumBy.id(Locators.CONVERSATION_TITLE_ID), 0));
        WebElement target = titles.stream()
                .filter(t -> title.isEmpty() || title.equals(t.getText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No conversation titled '" + title + "'"));
        target.click();
        waitForId(Locators.CHAT_INPUT_ID);
    }

    private static boolean isServerReachable(String baseUrl) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl.replaceAll("/+$", "") + "/status"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
