# Clean Mapped Exceptions for Java

The Java 25 implementation of the [Clean Mapped Exceptions concept](../../concept/README.md). The library has no runtime dependencies.

## Dependency

```xml
<dependency>
    <groupId>com.clean-arch-enablers</groupId>
    <artifactId>clean-mapped-exceptions</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

This is the development version. Build and install locally with `mvn clean install`; publication is a separate release step.

## Usage

```java
import com.cae.clean_mapped_exceptions.specifics.InputMappedException;
import com.cae.clean_mapped_exceptions.specifics.InternalMappedException;

if (customerId.isBlank()) {
    throw new InputMappedException(
            "A customer identifier is required",
            "customerId was blank");
}

try {
    repository.load(customerId);
} catch (RepositoryException original) {
    throw new InternalMappedException(
            "Customer cannot be loaded",
            "Repository lookup failed for customer " + customerId,
            original);
}
```

Choose a category according to its meaning in the workflow:

| Type in `com.cae.clean_mapped_exceptions.specifics` | Meaning |
| --- | --- |
| `InputMappedException` | Supplied input fails workflow requirements |
| `InternalMappedException` | An internal or dependency problem prevents execution |
| `NotFoundMappedException` | An expected resource cannot be found |
| `NotAuthorizedMappedException` | An identified actor lacks permission |
| `NotAuthenticatedMappedException` | Required actor identity has not been established |

The base `com.cae.clean_mapped_exceptions.MappedException` and every canonical type provide these constructors:

```java
new InputMappedException(publicMessage);
new InputMappedException(publicMessage, details);
new InputMappedException(publicMessage, original);
new InputMappedException(publicMessage, details, original);
```

All types remain extensible. Reuse a canonical category when it already conveys the required meaning. Already mapped failures can propagate directly when their meaning remains valid.

## Attribute and diagnostic contract

- `getBriefPublicMessage()`: a non-null description supplied by the caller. The caller is responsible for making it safe for its audience. Null public messages are rejected; empty strings are retained.
- `getDetails()`: `Optional<String>` diagnostic context. Null means absent; empty strings are retained.
- `getOriginalException()`: `Optional<Throwable>`, containing exactly the same object as `getCause()`. Null means absent. Nested causes and suppressed failures remain available to standard Java logging tools.
- `getMessage()`: diagnostic text, consisting of the public message, then ` | <details>` when details exist, then ` | Original: <original>` when an original exists. Absent attributes add no delimiters or literal null text.

Report only `getBriefPublicMessage()` publicly. General messages, details, causes, and traces can contain sensitive information. These classes do not assign HTTP statuses or serialize transport responses.

`getLinesFromStackTrace(int)` formats this exception's own frames.
`getLinesFromStackTraceFromOriginalException(int)` formats the original's own frames, or returns an empty list if none exists.
`getLinesFromStackTraceAsString(Throwable, int)` formats the supplied failure's own frames.

Frame helpers return immutable lists in stack order. The limit must be nonnegative. If truncated, a final `N hidden line(s)` entry follows the requested frames; this summary is not counted toward the limit. A zero limit returns only the summary when frames exist. An empty trace returns an empty list. Negative limits always throw `IllegalArgumentException`, including when there is no original. A null supplied failure throws `NullPointerException`.

The protected `getFullStackTraceAsString()` exposes Java's full diagnostic trace to subclasses, including causes and suppressed failures.

## Migrating from CAE Mapped Exceptions

| Old | New |
| --- | --- |
| Artifact `com.clean-arch-enablers:cae-mapped-exceptions` | `com.clean-arch-enablers:clean-mapped-exceptions` |
| Package `com.cae.mapped_exceptions` | `com.cae.clean_mapped_exceptions` |
| Package `com.cae.mapped_exceptions.specifics` | `com.cae.clean_mapped_exceptions.specifics` |
| Java 11 | Java 25 LTS |
| Original typed as `Exception` | Original typed as `Throwable` |
| Original stored separately from Java cause | Original attached as the standard Java cause |
| Protected attribute fields | Private immutable fields accessed through getters |
| Boxed `Integer` frame limits | Primitive `int` frame limits |
| Mutable frame lists | Immutable frame lists |

The canonical class names and getter names are retained. Replace the dependency and imports, then recompile consumers; this is a new artifact, not a binary-compatible replacement. Explicitly typed `Optional<Exception>` variables must become `Optional<Throwable>`. Subclasses should use getters instead of direct field access.

All categories now support all four constructors, including `InternalMappedException`, which previously exposed only the two constructors requiring details. Passing a null second argument requires a cast to `String` or `Throwable` to disambiguate the overloads; use the one-argument constructor for an absent attribute.

Null public messages are now rejected. Null optional attributes no longer append literal null text to the general message. Negative frame limits are now rejected consistently. Standard traces include the original causal chain and therefore differ from legacy traces. Serialized legacy exceptions are not a supported interchange format.

## Build and quality checks

From this directory, using JDK 25 and Maven 3.9 or newer:

```shell
mvn -B -ntp clean verify
```

The build enables all Java compiler lint checks except `serial` and fails on compiler or Javadoc warnings. Exception classes use Java's generated serialization version identifiers rather than explicit `serialVersionUID` fields. It packages the library, sources, and Javadoc. JaCoCo enforces zero missed instructions, branches, lines, methods, and complexity for each production class, without exclusions. Tests use JUnit and require no mocking agent.

Reports:

- `target/surefire-reports/`: unit test results.
- `target/site/jacoco/index.html`: browsable coverage.
- `target/site/jacoco/jacoco.xml`: machine-readable coverage for SonarQube.

To analyze against your SonarQube instance, provide its URL and a token through `SONAR_HOST_URL` and `SONAR_TOKEN`, then run:

```shell
mvn -B -ntp clean verify sonar:sonar
```

The scanner imports JaCoCo's XML report and waits for the server quality gate. A zero-issue requirement also requires an appropriate server quality profile and gate; configure the gate to reject any issues rather than relying only on severity ratings.
