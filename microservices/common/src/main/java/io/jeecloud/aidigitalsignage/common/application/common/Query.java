package io.jeecloud.aidigitalsignage.common.application.common;

/**
 * Marker interface for queries.
 * All queries should implement this interface to indicate they are read operations.
 * Part of CQRS pattern - separates queries from commands.
 */
public interface Query<T> {
    /**
     * Returns the expected result type of this query.
     */
    Class<T> getResultType();
}
