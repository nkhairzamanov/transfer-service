package org.superbank;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.h2.tools.Server;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.superbank.dataRepository.DataRepository;
import org.superbank.dataRepository.DataRepositoryImpl;
import org.superbank.endpoint.RestEndpoint;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.sql.SQLException;

public class TransferService {
    private final static Logger LOG = LoggerFactory.getLogger(TransferService.class);

    private final int httpPort;
    private final HttpServer server;
    private final Server h2TcpServer;

    TransferService() {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        final SessionFactory sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        final DataRepositoryImpl transferService = new DataRepositoryImpl(sessionFactory);
        httpPort = Integer.parseInt(System.getProperty("port.http.transfer-service", "8080"));
        final ResourceConfig config = new ResourceConfig(RestEndpoint.class);
        config.registerInstances(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(transferService).to(DataRepository.class);
            }
        });
        final URI baseUri = UriBuilder.fromUri("http://localhost/").port(httpPort).build();
        this.server = JdkHttpServerFactory.createHttpServer(baseUri, config);
        try {
            this.h2TcpServer = Server.createTcpServer().start(); //to be able to use an sql console in IDE
        } catch (SQLException e) {
            throw new TransferServiceRuntimeException(e);
        }
    }

    int getHttpPort() {
        return httpPort;
    }

    void stop() {
        this.server.stop(0);
        this.h2TcpServer.shutdown();
    }

    public static void main(String[] args) {
        LOG.info("Starting {}...", TransferService.class.getName());
        new TransferService();
        LOG.info("{} has started", TransferService.class.getName());
    }
}
