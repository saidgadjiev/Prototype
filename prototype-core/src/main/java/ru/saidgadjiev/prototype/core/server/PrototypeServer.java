package ru.saidgadjiev.prototype.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import ru.saidgadjiev.prototype.core.component.ComponentScan;
import ru.saidgadjiev.prototype.core.component.RestClass;

import java.util.Collection;

/**
 * Created by said on 12.09.2018.
 */
public class PrototypeServer {

    private final int port;

    private String restBasePackage;

    public PrototypeServer(int port) {
        this.port = port;
    }

    public void setRestBasePackage(String restBasePackage) {
        this.restBasePackage = restBasePackage;
    }

    public void run() throws Exception {
        ComponentScan componentScan = new ComponentScan();

        Collection<RestClass> restClassMap = componentScan.scan(restBasePackage);

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
                                    new StringDecoder(),
                                    new RequestParseDecoder(),
                                    new RequestDecoder(restClassMap),
                                    new ResponseWriteHandler(),
                                    new ResponseEncoder()
                            );
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
