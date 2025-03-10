package io.liftwizard.bundle.assets.cache;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.temporal.TemporalUnit;

import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.servlets.assets.AssetServlet;
import io.liftwizard.servlet.assets.cache.CacheAssetServlet;

public class CacheAssetsBundle
        extends AssetsBundle
{
    private final long amountToAdd;
    private final TemporalUnit temporalUnit;
    private final Clock clock;

    public CacheAssetsBundle(
            String resourcePath,
            String uriPath,
            String indexFile,
            String assetsName,
            long amountToAdd,
            TemporalUnit temporalUnit,
            Clock clock)
    {
        super(resourcePath, uriPath, indexFile, assetsName);
        this.amountToAdd = amountToAdd;
        this.temporalUnit = temporalUnit;
        this.clock = clock;
    }

    public CacheAssetsBundle(
            String resourcePath,
            String uriPath,
            String indexFile,
            String assetsName,
            long amountToAdd,
            TemporalUnit temporalUnit)
    {
        this(resourcePath, uriPath, indexFile, assetsName, amountToAdd, temporalUnit, Clock.systemUTC());
    }

    @Override
    protected AssetServlet createServlet()
    {
        return new CacheAssetServlet(
                this.getResourcePath(),
                this.getUriPath(),
                this.getIndexFile(),
                StandardCharsets.UTF_8,
                this.amountToAdd,
                this.temporalUnit,
                this.clock);
    }
}
