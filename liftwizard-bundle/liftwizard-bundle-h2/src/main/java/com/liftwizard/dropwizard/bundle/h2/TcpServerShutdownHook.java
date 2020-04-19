package com.liftwizard.dropwizard.bundle.h2;

import io.dropwizard.lifecycle.Managed;
import org.h2.tools.Server;

public class TcpServerShutdownHook implements Managed
{
    private final Server tcpServer;

    public TcpServerShutdownHook(Server tcpServer)
    {
        this.tcpServer = tcpServer;
    }

    @Override
    public void start()
    {
    }

    @Override
    public void stop()
    {
        this.tcpServer.stop();
    }
}
