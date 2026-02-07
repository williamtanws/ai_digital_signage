package io.jeecloud.aidigitalsignage.agent.domain;

/**
 * Channel enum representing distribution channels for agents.
 * This is a Value Object in DDD terms.
 */
public enum Channel {
    DIRECT("Direct Sales"),
    BROKER("Broker"),
    BANCASSURANCE("Bancassurance"),
    ONLINE("Online"),
    TELEMARKETING("Telemarketing");

    private final String description;

    Channel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

