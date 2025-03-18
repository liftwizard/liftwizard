package io.liftwizard.servlet.assets.cache;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.dropwizard.servlets.assets.AssetServlet;

public class CacheAssetServlet
        extends AssetServlet
{
    private final Clock clock;
    private final long amountToAdd;
    private final TemporalUnit temporalUnit;

    public CacheAssetServlet(
            String resourcePath,
            String uriPath,
            String indexFile,
            Charset charset,
            long amountToAdd,
            TemporalUnit temporalUnit,
            Clock clock)
    {
        super(resourcePath, uriPath, indexFile, charset);
        this.amountToAdd = amountToAdd;
        this.temporalUnit = temporalUnit;
        this.clock = clock;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        super.doGet(req, resp);

        Instant now = this.clock.instant();
        Instant expires = now.plus(this.amountToAdd, this.temporalUnit);

        long number = Duration.of(this.amountToAdd, this.temporalUnit).toSeconds();
        resp.setHeader("Cache-Control", "public, max-age=" + number);
        resp.setHeader("Expires", String.valueOf(expires.toEpochMilli()));
    }
}
