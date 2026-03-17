package yurykorzun.art.universe.common.pgnotify;

import java.util.regex.Pattern;

/**
 * Validates PostgreSQL NOTIFY channel names.
 * <p>
 * Channel names must start with a lowercase letter and contain only lowercase letters,
 * digits, and underscores. This prevents SQL injection when channel names are used
 * in {@code NOTIFY} / {@code LISTEN} statements, which do not support bind parameters.
 */
public final class PgChannelValidator {

    private static final Pattern VALID_CHANNEL = Pattern.compile("^[a-z][a-z0-9_]*$");

    public static boolean isValid(String channel) {
        return channel != null && VALID_CHANNEL.matcher(channel).matches();
    }

    public static void requireValid(String channel) {
        if (!isValid(channel)) {
            throw new IllegalArgumentException("Invalid NOTIFY channel name: '" + channel + "'");
        }
    }

    private PgChannelValidator() {}
}
