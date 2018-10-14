package ru.saidgadjiev.prototype.core.server;

import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import ru.saidgadjiev.prototype.core.bean.BeanFactory;
import ru.saidgadjiev.prototype.core.bean.BeanProcessor;
import ru.saidgadjiev.prototype.core.bean.ComponentScan;
import ru.saidgadjiev.prototype.core.http.session.SessionCreatorImpl;
import ru.saidgadjiev.prototype.core.http.session.SessionHolderImpl;
import ru.saidgadjiev.prototype.core.http.session.SessionManager;
import ru.saidgadjiev.prototype.core.http.session.SessionManagerImpl;
import ru.saidgadjiev.prototype.core.module.PrototypeModule;
import ru.saidgadjiev.prototype.core.service.AuthService;

import java.util.*;

/**
 * Created by said on 12.09.2018.
 */
public class PrototypeServer {

    private final int port;

    private ComponentScan componentScan;

    private Injector injector;

    private PrototypeServer(int port, ComponentScan componentScan, Injector injector) {
        this.port = port;
        this.componentScan = componentScan;
        this.injector = injector;
    }

    public void run() throws Exception {
        SessionManager sessionManager = new SessionManagerImpl(new SessionCreatorImpl(), new SessionHolderImpl());
        BeanProcessor beanProcessor = new BeanProcessor(
                componentScan,
                new BeanFactory(
                        injector, sessionManager
                )
        );

        DefaultEventExecutorGroup executorGroup = new DefaultEventExecutorGroup(10);

        GsonBuilder gsonBuilder = new GsonBuilder();
        RequestHandler requestHandler = new RequestHandler(
                beanProcessor,
                gsonBuilder,
                sessionManager
        );

        injector.injectMembers(requestHandler);

        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new HttpRequestDecoder(),
                                    new HttpObjectAggregator(1048576)
                            );

                            ch.pipeline().addLast(executorGroup, requestHandler);
                            ch.pipeline().addLast(new ResponseWriter());
                            ch.pipeline().addLast(new HttpResponseEncoder());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static class Builder {

        private final int port;

        private Set<String> packages = new HashSet<String>() {{
            add("ru.saidgadjiev.prototype.core");
        }};

        private Collection<AbstractModule> modules = new ArrayList<>();

        public Builder(int port) {
            this.port = port;
        }

        public Builder packages(String... packages) {
            Collections.addAll(this.packages, packages);

            return this;
        }

        public Builder modules(AbstractModule... modules) {
            Collections.addAll(this.modules, modules);

            return this;
        }

        public PrototypeServer build() {
            ComponentScan componentScan = new ComponentScan(packages);

            return new PrototypeServer(port, componentScan, createInjector(componentScan));
        }

        private Injector createInjector(ComponentScan componentScan) {
            modules.add(new PrototypeModule(componentScan.getRestClasses(), componentScan.getServiceClasses(), componentScan.getExpressionClasses()));

            return Guice.createInjector(modules);
        }
    }
}
