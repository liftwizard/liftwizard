package com.liftwizard.dropwizard.bundle.auth.filter;

import java.security.Principal;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import com.google.auto.service.AutoService;
import com.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import com.liftwizard.dropwizard.configuration.auth.filter.AuthFilterFactory;
import com.liftwizard.dropwizard.configuration.auth.filter.AuthFilterFactoryProvider;
import com.liftwizard.servlet.filter.mdc.keys.ClearMDCKeysFilter;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider.Binder;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.setup.Environment;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.mutable.ListAdapter;
import org.eclipse.jetty.servlet.FilterHolder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(PrioritizedBundle.class)
public class AuthFilterBundle
        implements PrioritizedBundle<Object>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilterBundle.class);

    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment)
    {
        AuthFilterFactoryProvider authFilterFactoryProvider =
                this.safeCastConfiguration(AuthFilterFactoryProvider.class, configuration);

        List<AuthFilterFactory> authFilterFactories = authFilterFactoryProvider.getAuthFilterFactories();

        List<AuthFilter<?, ? extends Principal>> authFilters = this.getAuthFilters(authFilterFactories);

        if (authFilters.isEmpty())
        {
            LOGGER.warn("{} disabled.", AuthFilterBundle.class.getSimpleName());
            return;
        }

        List<String> authFilterNames = authFilters
                .stream()
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .collect(Collectors.toList());

        LOGGER.info("Running {} with auth filters {}.", AuthFilterBundle.class.getSimpleName(), authFilterNames);

        environment.jersey().register(this.getAuthDynamicFeature(authFilters));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new Binder<>(Principal.class));

        Filter                  clearMDCFilter  = this.getClearMDCFilter(authFilterFactories);
        FilterHolder            filterHolder    = new FilterHolder(clearMDCFilter);
        EnumSet<DispatcherType> dispatcherTypes = EnumSet.of(DispatcherType.REQUEST);
        environment.getApplicationContext().addFilter(filterHolder, "/*", dispatcherTypes);

        LOGGER.info("Completing {}.", AuthFilterBundle.class.getSimpleName());
    }

    @Nonnull
    private List<AuthFilter<?, ? extends Principal>> getAuthFilters(List<AuthFilterFactory> authFilterFactories)
    {
        return authFilterFactories
                .stream()
                .map(AuthFilterFactory::createAuthFilter)
                .collect(Collectors.toList());
    }

    @Nonnull
    private AuthDynamicFeature getAuthDynamicFeature(List<AuthFilter<?, ? extends Principal>> authFilters)
    {
        ChainedAuthFilter chainedAuthFilter = new ChainedAuthFilter(authFilters);
        return new AuthDynamicFeature(chainedAuthFilter);
    }

    @Nonnull
    private Filter getClearMDCFilter(List<AuthFilterFactory> authFilterFactories)
    {
        ImmutableList<String> mdcKeys = ListAdapter.adapt(authFilterFactories)
                .flatCollect(AuthFilterFactory::getMDCKeys)
                .toImmutable();
        return new ClearMDCKeysFilter(mdcKeys);
    }
}
