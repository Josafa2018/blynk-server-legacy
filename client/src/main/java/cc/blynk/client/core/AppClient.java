package cc.blynk.client.core;

import java.io.File;
import java.util.Collections;
import java.util.Random;

import javax.net.ssl.SSLException;

import cc.blynk.client.handlers.AppClientReplayningMessageDecoder;
import cc.blynk.client.handlers.encoders.AppClientMessageEncoder;
import cc.blynk.server.core.stats.GlobalStats;
import cc.blynk.utils.properties.ServerProperties;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 11.03.15.
 */
public class AppClient extends BaseClient {

    protected SslContext sslCtx;

    public AppClient(String host, int port) {
        this(host, port, new Random(), new ServerProperties(Collections.emptyMap()));
    }

    protected AppClient(String host, int port, Random msgIdGenerator) {
        super(host, port, msgIdGenerator);
        log.info("Creating app Client. Host {}, Port : {}", host, port);
    }

    protected AppClient(String host, int port, Random msgIdGenerator, ServerProperties properties) {
        super(host, port, msgIdGenerator, properties);
        log.info("Creating app client. Host {}, sslPort : {}", host, port);
        File serverCert = makeCertificateFile("server.ssl.cert");
        File clientCert = makeCertificateFile("client.ssl.cert");
        File clientKey = makeCertificateFile("client.ssl.key");
        try {
            if (!serverCert.exists() || !clientCert.exists() || !clientKey.exists()) {
                log.info("Enabling one-way auth with no certs checks.");
                this.sslCtx = SslContextBuilder.forClient().sslProvider(SslProvider.JDK)
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
            } else {
                log.info("Enabling mutual auth.");
                String clientPass = props.getProperty("client.ssl.key.pass");
                this.sslCtx = SslContextBuilder.forClient()
                        .sslProvider(SslProvider.JDK)
                        .trustManager(serverCert)
                        .keyManager(clientCert, clientKey, clientPass)
                        .build();
            }
        } catch (SSLException e) {
            log.error("Error initializing SSL context. Reason : {}", e.getMessage());
            log.debug(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChannelInitializer<SocketChannel> getChannelInitializer() {
        return new ChannelInitializer<>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                if (sslCtx != null) {
                    pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                }
                pipeline.addLast(new AppClientReplayningMessageDecoder());
                pipeline.addLast(new AppClientMessageEncoder(new GlobalStats()));
            }
        };
    }
}
