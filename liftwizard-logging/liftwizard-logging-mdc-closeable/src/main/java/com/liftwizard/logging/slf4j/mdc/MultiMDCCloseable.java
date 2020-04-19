package com.liftwizard.logging.slf4j.mdc;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.MDC;

public final class MultiMDCCloseable implements AutoCloseable
{
    private final Set<String> keys = new LinkedHashSet<>();

    public void put(String key, String value)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("key parameter cannot be null");
        }

        if (!this.keys.add(key))
        {
            throw new IllegalArgumentException(key);
        }

        MDC.put(key, value);
    }

    @Override
    public void close()
    {
        this.keys.forEach(MDC::remove);
    }
}
