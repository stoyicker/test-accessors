package testaccessors.internal;

enum LogLevel {
    LEVEL_NONE("nothing"),
    LEVEL_ERROR("errors"),
    LEVEL_WARN("warnings"),
    LEVEL_NOTE("all");

    final String key;

    LogLevel(final String key) {
        this.key = key;
    }
}
