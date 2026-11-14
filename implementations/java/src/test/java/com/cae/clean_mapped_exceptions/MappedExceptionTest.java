package com.cae.clean_mapped_exceptions;

import com.cae.clean_mapped_exceptions.specifics.InputMappedException;
import com.cae.clean_mapped_exceptions.specifics.InternalMappedException;
import com.cae.clean_mapped_exceptions.specifics.NotAuthenticatedMappedException;
import com.cae.clean_mapped_exceptions.specifics.NotAuthorizedMappedException;
import com.cae.clean_mapped_exceptions.specifics.NotFoundMappedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappedExceptionTest {

    private static final String PUBLIC_MESSAGE = "Customer cannot be loaded";
    private static final String DETAILS = "Dependency lookup failed for customer 42";
    private static final Throwable ORIGINAL = new IllegalStateException("Dependency unavailable");
    private static final StackTraceElement FIRST = new StackTraceElement("example.Lookup", "find", "Lookup.java", 10);
    private static final StackTraceElement SECOND = new StackTraceElement("example.Workflow", "run", "Workflow.java", 20);
    private static final StackTraceElement THIRD = new StackTraceElement("example.Adapter", "handle", "Adapter.java", 30);

    @ParameterizedTest
    @MethodSource("exceptionCases")
    void preservesAttributesAndStandardCause(MappedException exception, String message, String details, Throwable cause) {
        assertAll(
                () -> assertInstanceOf(RuntimeException.class, exception),
                () -> assertEquals(PUBLIC_MESSAGE, exception.getBriefPublicMessage()),
                () -> assertEquals(Optional.ofNullable(details), exception.getDetails()),
                () -> assertEquals(Optional.ofNullable(cause), exception.getOriginalException()),
                () -> assertSame(cause, exception.getCause()),
                () -> assertEquals(message, exception.getMessage()));
    }

    static Stream<Arguments> exceptionCases() {
        return Stream.of(
                Arguments.of(new MappedException(PUBLIC_MESSAGE), PUBLIC_MESSAGE, null, null),
                Arguments.of(new MappedException(PUBLIC_MESSAGE, DETAILS), PUBLIC_MESSAGE + " | " + DETAILS, DETAILS, null),
                Arguments.of(new MappedException(PUBLIC_MESSAGE, ORIGINAL), PUBLIC_MESSAGE + " | Original: " + ORIGINAL, null, ORIGINAL),
                Arguments.of(new MappedException(PUBLIC_MESSAGE, DETAILS, ORIGINAL), PUBLIC_MESSAGE + " | " + DETAILS + " | Original: " + ORIGINAL, DETAILS, ORIGINAL),
                Arguments.of(new InputMappedException(PUBLIC_MESSAGE), PUBLIC_MESSAGE, null, null),
                Arguments.of(new InputMappedException(PUBLIC_MESSAGE, DETAILS), PUBLIC_MESSAGE + " | " + DETAILS, DETAILS, null),
                Arguments.of(new InputMappedException(PUBLIC_MESSAGE, ORIGINAL), PUBLIC_MESSAGE + " | Original: " + ORIGINAL, null, ORIGINAL),
                Arguments.of(new InputMappedException(PUBLIC_MESSAGE, DETAILS, ORIGINAL), PUBLIC_MESSAGE + " | " + DETAILS + " | Original: " + ORIGINAL, DETAILS, ORIGINAL),
                Arguments.of(new InternalMappedException(PUBLIC_MESSAGE), PUBLIC_MESSAGE, null, null),
                Arguments.of(new InternalMappedException(PUBLIC_MESSAGE, DETAILS), PUBLIC_MESSAGE + " | " + DETAILS, DETAILS, null),
                Arguments.of(new InternalMappedException(PUBLIC_MESSAGE, ORIGINAL), PUBLIC_MESSAGE + " | Original: " + ORIGINAL, null, ORIGINAL),
                Arguments.of(new InternalMappedException(PUBLIC_MESSAGE, DETAILS, ORIGINAL), PUBLIC_MESSAGE + " | " + DETAILS + " | Original: " + ORIGINAL, DETAILS, ORIGINAL),
                Arguments.of(new NotFoundMappedException(PUBLIC_MESSAGE), PUBLIC_MESSAGE, null, null),
                Arguments.of(new NotFoundMappedException(PUBLIC_MESSAGE, DETAILS), PUBLIC_MESSAGE + " | " + DETAILS, DETAILS, null),
                Arguments.of(new NotFoundMappedException(PUBLIC_MESSAGE, ORIGINAL), PUBLIC_MESSAGE + " | Original: " + ORIGINAL, null, ORIGINAL),
                Arguments.of(new NotFoundMappedException(PUBLIC_MESSAGE, DETAILS, ORIGINAL), PUBLIC_MESSAGE + " | " + DETAILS + " | Original: " + ORIGINAL, DETAILS, ORIGINAL),
                Arguments.of(new NotAuthorizedMappedException(PUBLIC_MESSAGE), PUBLIC_MESSAGE, null, null),
                Arguments.of(new NotAuthorizedMappedException(PUBLIC_MESSAGE, DETAILS), PUBLIC_MESSAGE + " | " + DETAILS, DETAILS, null),
                Arguments.of(new NotAuthorizedMappedException(PUBLIC_MESSAGE, ORIGINAL), PUBLIC_MESSAGE + " | Original: " + ORIGINAL, null, ORIGINAL),
                Arguments.of(new NotAuthorizedMappedException(PUBLIC_MESSAGE, DETAILS, ORIGINAL), PUBLIC_MESSAGE + " | " + DETAILS + " | Original: " + ORIGINAL, DETAILS, ORIGINAL),
                Arguments.of(new NotAuthenticatedMappedException(PUBLIC_MESSAGE), PUBLIC_MESSAGE, null, null),
                Arguments.of(new NotAuthenticatedMappedException(PUBLIC_MESSAGE, DETAILS), PUBLIC_MESSAGE + " | " + DETAILS, DETAILS, null),
                Arguments.of(new NotAuthenticatedMappedException(PUBLIC_MESSAGE, ORIGINAL), PUBLIC_MESSAGE + " | Original: " + ORIGINAL, null, ORIGINAL),
                Arguments.of(new NotAuthenticatedMappedException(PUBLIC_MESSAGE, DETAILS, ORIGINAL), PUBLIC_MESSAGE + " | " + DETAILS + " | Original: " + ORIGINAL, DETAILS, ORIGINAL));
    }

    @ParameterizedTest
    @MethodSource("exceptionCases")
    void survivesSerialization(MappedException exception, String message, String details, Throwable cause)
            throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var output = new ObjectOutputStream(bytes)) {
            output.writeObject(exception);
        }
        MappedException restored;
        try (var input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = assertInstanceOf(MappedException.class, input.readObject());
        }
        assertAll(
                () -> assertEquals(exception.getClass(), restored.getClass()),
                () -> assertEquals(PUBLIC_MESSAGE, restored.getBriefPublicMessage()),
                () -> assertEquals(message, restored.getMessage()),
                () -> assertEquals(Optional.ofNullable(details), restored.getDetails()),
                () -> assertEquals(List.of(exception.getStackTrace()), List.of(restored.getStackTrace())));
        if (cause == null) {
            assertTrue(restored.getOriginalException().isEmpty());
            assertNull(restored.getCause());
        } else {
            var restoredCause = restored.getOriginalException().orElseThrow();
            assertAll(
                    () -> assertSame(restoredCause, restored.getCause()),
                    () -> assertEquals(cause.getClass(), restoredCause.getClass()),
                    () -> assertEquals(cause.getMessage(), restoredCause.getMessage()));
        }
    }

    @Test
    void acceptsAbsentOptionalAttributesWithoutNullTextInDiagnosticMessage() {
        var nullDetails = new MappedException(PUBLIC_MESSAGE, (String) null);
        var nullOriginal = new MappedException(PUBLIC_MESSAGE, (Throwable) null);
        var bothNull = new MappedException(PUBLIC_MESSAGE, null, null);
        var detailsOnly = new MappedException(PUBLIC_MESSAGE, DETAILS, null);
        var originalOnly = new MappedException(PUBLIC_MESSAGE, null, ORIGINAL);
        assertAll(
                () -> assertEquals(PUBLIC_MESSAGE, nullDetails.getMessage()),
                () -> assertEquals(PUBLIC_MESSAGE, nullOriginal.getMessage()),
                () -> assertEquals(PUBLIC_MESSAGE, bothNull.getMessage()),
                () -> assertEquals(PUBLIC_MESSAGE + " | " + DETAILS, detailsOnly.getMessage()),
                () -> assertEquals(PUBLIC_MESSAGE + " | Original: " + ORIGINAL, originalOnly.getMessage()));
    }

    @Test
    void preservesEmptyAndUnicodeAttributesVerbatim() {
        var exception = new InputMappedException("Entrada inválida", "");
        var emptyMessage = new MappedException("");
        assertAll(
                () -> assertEquals("Entrada inválida", exception.getBriefPublicMessage()),
                () -> assertEquals(Optional.of(""), exception.getDetails()),
                () -> assertEquals("Entrada inválida | ", exception.getMessage()),
                () -> assertEquals("", emptyMessage.getBriefPublicMessage()));
    }

    @Test
    void rejectsNullPublicMessagesInEveryConstructor() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new MappedException(null)),
                () -> assertThrows(NullPointerException.class, () -> new MappedException(null, DETAILS)),
                () -> assertThrows(NullPointerException.class, () -> new MappedException(null, ORIGINAL)),
                () -> assertThrows(NullPointerException.class, () -> new MappedException(null, DETAILS, ORIGINAL)));
    }

    @Test
    void retainsThrowableCausesAndNestedMappedFailures() {
        var root = new AssertionError("Dependency invariant failed");
        var inner = new InternalMappedException("Dependency failed", root);
        var outer = new InternalMappedException(PUBLIC_MESSAGE, inner);
        assertAll(
                () -> assertSame(inner, outer.getCause()),
                () -> assertSame(inner, outer.getOriginalException().orElseThrow()),
                () -> assertSame(root, inner.getCause()),
                () -> assertEquals(PUBLIC_MESSAGE, outer.getBriefPublicMessage()));
    }

    @ParameterizedTest
    @MethodSource("lineLimits")
    void formatsOwnAndOriginalFramesWithExactTruncation(int limit, List<String> expected) {
        var original = new IllegalArgumentException("Original");
        original.setStackTrace(new StackTraceElement[]{FIRST, SECOND, THIRD});
        var exception = new MappedException(PUBLIC_MESSAGE, original);
        exception.setStackTrace(new StackTraceElement[]{FIRST, SECOND, THIRD});
        assertAll(
                () -> assertEquals(expected, exception.getLinesFromStackTrace(limit)),
                () -> assertEquals(expected, exception.getLinesFromStackTraceFromOriginalException(limit)),
                () -> assertEquals(expected, exception.getLinesFromStackTraceAsString(original, limit)));
    }

    static Stream<Arguments> lineLimits() {
        return Stream.of(
                Arguments.of(0, List.of("3 hidden line(s)")),
                Arguments.of(1, List.of(FIRST.toString(), "2 hidden line(s)")),
                Arguments.of(2, List.of(FIRST.toString(), SECOND.toString(), "1 hidden line(s)")),
                Arguments.of(3, List.of(FIRST.toString(), SECOND.toString(), THIRD.toString())),
                Arguments.of(4, List.of(FIRST.toString(), SECOND.toString(), THIRD.toString())),
                Arguments.of(Integer.MAX_VALUE, List.of(FIRST.toString(), SECOND.toString(), THIRD.toString())));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, Integer.MAX_VALUE})
    void handlesEmptyTracesAndAbsentOriginal(int limit) {
        var empty = new IllegalStateException("No frames");
        empty.setStackTrace(new StackTraceElement[0]);
        var exception = new MappedException(PUBLIC_MESSAGE, empty);
        exception.setStackTrace(new StackTraceElement[0]);
        var noOriginal = new MappedException(PUBLIC_MESSAGE);
        assertAll(
                () -> assertEquals(List.of(), exception.getLinesFromStackTrace(limit)),
                () -> assertEquals(List.of(), exception.getLinesFromStackTraceFromOriginalException(limit)),
                () -> assertEquals(List.of(), noOriginal.getLinesFromStackTraceFromOriginalException(limit)));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, Integer.MIN_VALUE})
    void rejectsNegativeLimitsWithAndWithoutOriginal(int limit) {
        var exception = new MappedException(PUBLIC_MESSAGE, ORIGINAL);
        var noOriginal = new MappedException(PUBLIC_MESSAGE);
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> exception.getLinesFromStackTrace(limit)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> exception.getLinesFromStackTraceFromOriginalException(limit)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> noOriginal.getLinesFromStackTraceFromOriginalException(limit)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> exception.getLinesFromStackTraceAsString(ORIGINAL, limit)));
    }

    @Test
    void rejectsNullFailureWhenFormattingFrames() {
        var exception = new MappedException(PUBLIC_MESSAGE);
        assertThrows(NullPointerException.class, () -> exception.getLinesFromStackTraceAsString(null, 1));
    }

    @Test
    void keepsMappingFramesSeparateFromOriginalFrames() {
        var original = new IllegalStateException("Dependency");
        original.setStackTrace(new StackTraceElement[]{FIRST});
        var exception = new MappedException(PUBLIC_MESSAGE, original);
        exception.setStackTrace(new StackTraceElement[]{SECOND});
        assertAll(
                () -> assertEquals(List.of(SECOND.toString()), exception.getLinesFromStackTrace(1)),
                () -> assertEquals(List.of(FIRST.toString()),
                        exception.getLinesFromStackTraceFromOriginalException(1)));
    }

    @Test
    void fullTraceIncludesMappingLocationOriginalChainAndSuppressedFailure() {
        var root = new IllegalArgumentException("Root cause");
        root.setStackTrace(new StackTraceElement[]{THIRD});
        var original = new IllegalStateException("Dependency failed", root);
        original.setStackTrace(new StackTraceElement[]{FIRST});
        var exception = new InternalMappedException(PUBLIC_MESSAGE, DETAILS, original);
        exception.setStackTrace(new StackTraceElement[]{SECOND});
        var suppressed = new IllegalStateException("Cleanup failed");
        suppressed.setStackTrace(new StackTraceElement[0]);
        exception.addSuppressed(suppressed);

        var trace = exception.getFullStackTraceAsString();

        assertAll(
                () -> assertTrue(trace.startsWith(exception.toString())),
                () -> assertTrue(trace.contains(SECOND.toString())),
                () -> assertTrue(trace.contains("Caused by: " + original)),
                () -> assertTrue(trace.contains(FIRST.toString())),
                () -> assertTrue(trace.contains("Caused by: " + root)),
                () -> assertTrue(trace.contains(THIRD.toString())),
                () -> assertTrue(trace.contains("Suppressed: " + suppressed)),
                () -> assertFalse(exception.getBriefPublicMessage().contains(DETAILS)),
                () -> assertFalse(exception.getBriefPublicMessage().contains(original.getMessage())));
    }

    @Test
    void fullTraceWorksWithoutAnOriginalFailure() {
        var exception = new MappedException(PUBLIC_MESSAGE);
        exception.setStackTrace(new StackTraceElement[]{FIRST});
        var trace = exception.getFullStackTraceAsString();
        assertEquals(exception + System.lineSeparator() + "\tat " + FIRST + System.lineSeparator(), trace);
    }
}
