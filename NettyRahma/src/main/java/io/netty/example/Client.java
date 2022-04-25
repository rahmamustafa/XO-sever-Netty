package io.netty.example;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public final class Client {
    static final String HOST = "127.0.0.1";
    static final int PORT = 8000;


    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group) // Set EventLoopGroup to handle all eventsf for client.
                    .channel(NioSocketChannel.class)// Use NIO to accept new connections.
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new StringDecoder());
                            p.addLast(new StringEncoder());

                            // This is our custom client handler which will have logic for chat.
                            p.addLast(new  ClientHandler());

                        }
                    });


            // Wait until the connection is closed.


            ChannelFuture f = b.connect(HOST, PORT).sync();
            Channel channel = f.sync().channel();
            channel.writeAndFlush("Start");
            channel.flush();

            // Start the client.

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }
    }
}
class  ClientHandler extends SimpleChannelInboundHandler<String> {

    public static boolean start = false;
    public static String Letter;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Message from Server: " + msg);
        String str = msg.toString();
        Scanner myObj = new Scanner(System.in);
        if (str.startsWith("Game started, your Turn" )|| str.startsWith("your Turn")){
            Letter = str.substring(str.length() - 1);
            //System.out.println("Game Started, Player " + Letter);
            start=true;
            while(ClientHandler.start) {
                System.out.println("enter pos");
                String input = myObj.nextLine();
                if (input.matches("[1-9]")) {
                    ctx.writeAndFlush(input +","+ ClientHandler.Letter );
                    ctx.flush();
                    break;
                } else
                    System.out.println("enter valid num");
            }
        }

        else {
            if(str.startsWith("this ")){
                System.out.println("Full cell enter again");
                start=true;
                while(ClientHandler.start) {
                    System.out.println("enter pos");
                    String input = myObj.nextLine();
                    if (input.matches("[1-9]")) {
                        ctx.writeAndFlush(input +","+ ClientHandler.Letter );
                        ctx.flush();
                        break;
                    } else
                        System.out.println("enter valid num");
                }
            }
            start=false;
        }


    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, String s) throws Exception {

    }

}