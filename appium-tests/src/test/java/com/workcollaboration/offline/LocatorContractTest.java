package com.workcollaboration.offline;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.workcollaboration.support.Locators;
import com.workcollaboration.support.TestConfig;

/**
 * Guards the end-to-end tests against locator drift: every app resource id they use must be
 * declared in the Android layouts. Runs without a device or Appium server.
 */
class LocatorContractTest {

    private static final Pattern DECLARED_ID = Pattern.compile("@[+]id/([A-Za-z0-9_]+)");
    private static final String APP_ID_PREFIX = Locators.APP_PACKAGE + ":id/";

    private static Set<String> declaredIds;

    @BeforeAll
    static void readLayouts() throws IOException {
        Path layoutDir = TestConfig.layoutDir();
        assertTrue(Files.isDirectory(layoutDir), "Layout directory not found: " + layoutDir);

        declaredIds = new HashSet<>();
        try (Stream<Path> files = Files.list(layoutDir)) {
            for (Path xml : files.filter(p -> p.toString().endsWith(".xml")).toList()) {
                Matcher m = DECLARED_ID.matcher(Files.readString(xml));
                while (m.find()) {
                    declaredIds.add(m.group(1));
                }
            }
        }
        assertFalse(declaredIds.isEmpty(), "No @+id declarations found in " + layoutDir);
    }

    @TestFactory
    Stream<DynamicTest> everyLocatorIdExistsInLayouts() {
        List<Field> idFields = Stream.of(Locators.class.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()) && f.getType() == String.class)
                .filter(f -> f.getName().endsWith("_ID"))
                .toList();
        assertFalse(idFields.isEmpty(), "Locators declares no *_ID constants");

        return idFields.stream().map(f -> DynamicTest.dynamicTest(f.getName(), () -> {
            String value = (String) f.get(null);
            assertTrue(value.startsWith(APP_ID_PREFIX), f.getName() + " is not an app id: " + value);
            String id = value.substring(APP_ID_PREFIX.length());
            assertTrue(declaredIds.contains(id),
                    f.getName() + " -> '" + id + "' is not declared in any res/layout/*.xml");
        }));
    }
}
