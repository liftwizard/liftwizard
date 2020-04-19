package com.liftwizard.logging.logback.filter.requesturl;

import java.util.Objects;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class RequestUrlFilter extends Filter<IAccessEvent>
{
    private final ImmutableList<String> bannedUrls;

    public RequestUrlFilter(Iterable<String> bannedUrls)
    {
        Objects.requireNonNull(bannedUrls);
        this.bannedUrls = Lists.immutable.withAll(bannedUrls);
    }

    @Override
    public FilterReply decide(IAccessEvent event)
    {
        String requestURL = event.getRequestURL();

        return this.bannedUrls.anySatisfy(requestURL::contains)
                ? FilterReply.DENY
                : FilterReply.NEUTRAL;
    }
}
