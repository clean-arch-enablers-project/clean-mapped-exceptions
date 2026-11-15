package com.cae.clean_mapped_exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An unchecked failure with workflow meaning and separate public and diagnostic attributes.
 *
 * <p>Only {@link #getBriefPublicMessage()} is intended for public reporting.
 * The general message, details, cause and stack traces can contain sensitive information.
 * Subclasses may specialize this type without selecting a transport or response format.
 */
public class MappedException extends RuntimeException {

    /**
     * The description intended for public exposure.
     *
     * @serial the description intended for public exposure
     */
    private final String briefPublicMessage;

    /**
     * Internal diagnostic context, or null if absent.
     *
     * @serial internal diagnostic context, or null if absent
     */
    private final String details;

    /**
     * Creates a failure with a public description.
     *
     * @param briefPublicMessage the non-null public description
     * @throws NullPointerException if the public description is null
     */
    public MappedException(String briefPublicMessage) {
        this(briefPublicMessage, null, null);
    }

    /**
     * Creates a failure with public and diagnostic descriptions.
     *
     * @param briefPublicMessage the non-null public description
     * @param details diagnostic context, or null if absent
     * @throws NullPointerException if the public description is null
     */
    public MappedException(String briefPublicMessage, String details) {
        this(briefPublicMessage, details, null);
    }

    /**
     * Maps an underlying failure to a public description.
     *
     * @param briefPublicMessage the non-null public description
     * @param originalException the underlying failure, or null if absent
     * @throws NullPointerException if the public description is null
     */
    public MappedException(String briefPublicMessage, Throwable originalException) {
        this(briefPublicMessage, null, originalException);
    }

    /**
     * Creates a failure with all attributes, retaining the original as the Java cause.
     *
     * @param briefPublicMessage the non-null public description
     * @param details diagnostic context, or null if absent
     * @param originalException the underlying failure, or null if absent
     * @throws NullPointerException if the public description is null
     */
    public MappedException(String briefPublicMessage, String details, Throwable originalException) {
        super(diagnosticMessage(briefPublicMessage, details, originalException), originalException);
        this.briefPublicMessage = briefPublicMessage;
        this.details = details;
    }

    private static String diagnosticMessage(String publicMessage, String details, Throwable original) {
        var message = new StringBuilder(Objects.requireNonNull(publicMessage, "briefPublicMessage"));
        if (details != null) {
            message.append(" | ").append(details);
        }
        if (original != null) {
            message.append(" | Original: ").append(original);
        }
        return message.toString();
    }

    /**
     * Returns the description intended for public exposure.
     *
     * @return the public description, without details or cause information
     */
    public String getBriefPublicMessage() {
        return briefPublicMessage;
    }

    /**
     * Returns internal diagnostic context.
     *
     * @return the details, or an empty optional if absent
     */
    public Optional<String> getDetails() {
        return Optional.ofNullable(details);
    }

    /**
     * Returns the original failure, identical to the Java cause.
     *
     * @return the underlying failure, or an empty optional if absent
     */
    public Optional<Throwable> getOriginalException() {
        return Optional.ofNullable(getCause());
    }

    /**
     * Returns frames from the original failure's own stack trace.
     *
     * @param numberOfLines the maximum number of frames, at least zero
     * @return the frames and, if truncated, a hidden-frame count; empty if no cause exists
     * @throws IllegalArgumentException if the limit is negative, even when no cause exists
     */
    public List<String> getLinesFromStackTraceFromOriginalException(int numberOfLines) {
        validateLineLimit(numberOfLines);
        return getOriginalException()
                .map(original -> getLinesFromStackTraceAsString(original, numberOfLines))
                .orElseGet(List::of);
    }

    /**
     * Returns frames from this failure's own stack trace.
     *
     * @param numberOfLines the maximum number of frames, at least zero
     * @return the frames and, if truncated, a hidden-frame count
     * @throws IllegalArgumentException if the limit is negative
     */
    public List<String> getLinesFromStackTrace(int numberOfLines) {
        return getLinesFromStackTraceAsString(this, numberOfLines);
    }

    /**
     * Formats the supplied failure's own frames, without traversing its causal chain.
     *
     * @param exception the non-null failure to inspect
     * @param numberOfLines the maximum number of frames, at least zero
     * @return the frames and, if truncated, a final "N hidden line(s)" entry
     * @throws NullPointerException if the failure is null
     * @throws IllegalArgumentException if the limit is negative
     */
    public List<String> getLinesFromStackTraceAsString(Throwable exception, int numberOfLines) {
        Objects.requireNonNull(exception, "exception");
        validateLineLimit(numberOfLines);
        var frames = exception.getStackTrace();
        var lines = new ArrayList<String>();
        for (int index = 0; index < Math.min(frames.length, numberOfLines); index++) {
            lines.add(frames[index].toString());
        }
        if (frames.length > numberOfLines) {
            lines.add((frames.length - numberOfLines) + " hidden line(s)");
        }
        return List.copyOf(lines);
    }

    private static void validateLineLimit(int numberOfLines) {
        if (numberOfLines < 0) {
            throw new IllegalArgumentException("numberOfLines must be at least zero");
        }
    }

    /**
     * Formats the complete diagnostic trace, including causes and suppressed failures.
     *
     * @return the standard Java stack trace
     */
    protected String getFullStackTraceAsString() {
        var writer = new StringWriter();
        try (var printer = new PrintWriter(writer)) {
            printStackTrace(printer);
        }
        return writer.toString();
    }
}
