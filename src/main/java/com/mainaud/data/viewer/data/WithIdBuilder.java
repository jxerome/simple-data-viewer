package com.mainaud.data.viewer.data;

import java.util.UUID;

public interface WithIdBuilder<T extends WithIdBuilder> {

    T withId(UUID id);

    default T withRandomId() {
        return withId(UUID.randomUUID());
    }
}
