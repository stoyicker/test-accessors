package testaccessors.internal;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import static testaccessors.internal.AnnotationProcessor.OPTION_KEY_LOG_LEVEL;

abstract class Logger {
    private final Messager messager;
    private final LogLevel logLevel;

    Logger(final Messager messager, final LogLevel logLevel) {
        this.messager = messager;
        this.logLevel = logLevel;
    }

    final void error(CharSequence message) {
        error(message, null);
    }

    final void error(CharSequence message, Element culprit) {
        if (logLevel.ordinal() >= LogLevel.LEVEL_ERROR.ordinal()) {
            messager.printMessage(Diagnostic.Kind.ERROR, message + SUFFIX_SKIP_VERIFICATION, culprit);
        }
    }

    final void warn(CharSequence message) {
        warn(message, null);
    }

    final void warn(CharSequence message, Element culprit) {
        if (logLevel.ordinal() >= LogLevel.LEVEL_WARN.ordinal()) {
            messager.printMessage(Diagnostic.Kind.WARNING, message + SUFFIX_SKIP_VERIFICATION, culprit);
        }
    }

    final void note(CharSequence message) {
        note(message, null);
    }

    final void note(CharSequence message, Element culprit) {
        if (logLevel.ordinal() >= LogLevel.LEVEL_NOTE.ordinal()) {
            messager.printMessage(Diagnostic.Kind.NOTE, message + SUFFIX_SKIP_VERIFICATION, culprit);
        }
    }

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

    private static final String SUFFIX_SKIP_VERIFICATION =
            "\nTo ignore this and/or other verification messages, use the option " + OPTION_KEY_LOG_LEVEL;
}
