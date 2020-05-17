package com.aboni.utils;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class QueryByDate implements Query {

    private final Instant from;
    private final Instant to;

    public QueryByDate(@NotNull Instant from, @NotNull Instant to) {
        this.from = from;
        this.to = to;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }
}
