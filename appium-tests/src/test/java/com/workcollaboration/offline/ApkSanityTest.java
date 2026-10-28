package com.workcollaboration.offline;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.Test;

import com.workcollaboration.support.TestConfig;

/** Verifies the APK the e2e suite installs is present and structurally valid. */
class ApkSanityTest {

    @Test
    void apkExistsAndContainsManifestAndDex() throws Exception {
        Path apk = TestConfig.appPath();
        assertTrue(Files.isRegularFile(apk), "APK not found: " + apk);

        try (ZipFile zip = new ZipFile(apk.toFile())) {
            assertNotNull(zip.getEntry("AndroidManifest.xml"), "APK has no AndroidManifest.xml");
            assertNotNull(zip.getEntry("classes.dex"), "APK has no classes.dex");
        }
    }
}
