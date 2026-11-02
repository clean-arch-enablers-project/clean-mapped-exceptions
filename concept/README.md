# Clean Mapped Exceptions

This concept defines the canonical semantics for exceptions that an application workflow might encounter.

At a sufficiently abstract level, recurring patterns emerge.

An exception occurs because of...

- ...a **problematic input** payload
- ...an **internal problem** (timeout, faulty code, unavailable dependency, etc.)
- ...an expected resource **not being found**
- ...an actor attempting to execute an **unauthorized** action
- ...an **unauthenticated** actor attempting to access protected resources

All of these categories of failure are foreseeable when designing a workflow.

For example, one might not know exactly how an API call might fail, but failure remains a foreseeable possibility. Designing the workflow to represent such failures through mapped exceptions makes them easier to understand and act upon, whether programmatically or during troubleshooting sessions.

The classification should reflect the **semantic meaning of a failure within its workflow context**, rather than merely its underlying technical cause.

To establish a common vocabulary for these failures, this concept defines five canonical exception types:

- **Input Mapped Exception**
- **Internal Mapped Exception**
- **Not Found Mapped Exception**
- **Not Authorized Mapped Exception**
- **Not Authenticated Mapped Exception**

These types are intended to be freely and extensively reused throughout workflows in any application, regardless of programming language or underlying technology.

Highly specialized scenarios might justify extending the canonical types, but in most cases, specialization should be achieved through contextual information rather than additional exception classes.

The canonical types express the nature of mapped exceptions. However, to communicate the specific circumstances of a failure, each mapped exception may carry up to three kinds of information:

- Brief Public Message
- Details
- Original

### Brief Public Message

A concise, human-readable description of the failure, designed to be safe for public exposure.

It should communicate what went wrong without revealing sensitive information or unnecessary implementation details.

### Details

Additional contextual information intended to help understand and diagnose the failure.

Unlike the public message, details may contain technical or sensitive information and should not be assumed safe for public exposure.

The purpose is to provide sufficient context for troubleshooting, observability, or programmatic decision-making.

### Original

The underlying exception that originated the mapped exception, when applicable.

Not all mapped exceptions originate from another exception. However, when they do, preserving the original allows the causal chain and diagnostic information, such as the original stack trace, to remain accessible.

---

<p align="center">CAE: clean software made easy.</p>