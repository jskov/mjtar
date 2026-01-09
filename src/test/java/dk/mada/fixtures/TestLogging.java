package dk.mada.fixtures;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.logging.LogManager;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/// Reconfigures logging for tests.
public final class TestLogging implements BeforeAllCallback {
    /// Flag for initialization.
    private static boolean initialized = false;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!initialized) {
            try (InputStream configFile = TestLogging.class.getResourceAsStream("/test-logging.properties")) {
                LogManager.getLogManager().updateConfiguration(configFile, null);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to load logging properties", e);
            }
        }
    }
}
