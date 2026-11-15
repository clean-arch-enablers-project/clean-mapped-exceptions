package com.cae.clean_mapped_exceptions.specifics;

import com.cae.clean_mapped_exceptions.MappedException;

/**
 * The required actor identity has not been established.
 *
 * <p>Classification is determined by workflow context and does not imply a transport status.
 */
public class NotAuthenticatedMappedException extends MappedException {

    /**
     * Creates a failure with a public description.
     *
     * @param briefPublicMessage the non-null public description
     * @throws NullPointerException if the public description is null
     */
    public NotAuthenticatedMappedException(String briefPublicMessage) {
        super(briefPublicMessage);
    }

    /**
     * Creates a failure with diagnostic context.
     *
     * @param briefPublicMessage the non-null public description
     * @param details diagnostic context, or null if absent
     * @throws NullPointerException if the public description is null
     */
    public NotAuthenticatedMappedException(String briefPublicMessage, String details) {
        super(briefPublicMessage, details);
    }

    /**
     * Maps an underlying failure.
     *
     * @param briefPublicMessage the non-null public description
     * @param originalException the underlying failure, or null if absent
     * @throws NullPointerException if the public description is null
     */
    public NotAuthenticatedMappedException(String briefPublicMessage, Throwable originalException) {
        super(briefPublicMessage, originalException);
    }

    /**
     * Maps an underlying failure with diagnostic context.
     *
     * @param briefPublicMessage the non-null public description
     * @param details diagnostic context, or null if absent
     * @param originalException the underlying failure, or null if absent
     * @throws NullPointerException if the public description is null
     */
    public NotAuthenticatedMappedException(String briefPublicMessage, String details, Throwable originalException) {
        super(briefPublicMessage, details, originalException);
    }
}
