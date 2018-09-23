package ru.saidgadjiev.prototype.core.server;

import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
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
import ru.saidgadjiev.prototype.core.component.BeanFactory;
import ru.saidgadjiev.prototype.core.component.BeanProcessor;
import ru.saidgadjiev.prototype.core.component.ComponentScan;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by said on 12.09.2018.
 */
public class PrototypeServer {

    private final int port;

    private String restBasePackage;

    private Injector injector;

    public PrototypeServer(int port) {
        this.port = port;
    }

    public void setRestBasePackage(String restBasePackage) {
        this.restBasePackage = restBasePackage;
    }

    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public void run() throws Exception {
        ComponentScan componentScan = new ComponentScan(restBasePackage);

        BeanProcessor beanProcessor = new BeanProcessor(componentScan, new BeanFactory(injector));

        DefaultEventExecutorGroup executorGroup = new DefaultEventExecutorGroup(10);

        GsonBuilder gsonBuilder = new GsonBuilder();

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

                            ch.pipeline().addLast(executorGroup, new RequestHandler(beanProcessor, gsonBuilder));
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
}
