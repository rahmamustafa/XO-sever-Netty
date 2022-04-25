package io.netty.example;
import java.util.*;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
public final class Server {
    // Port where chat server will listen for connections.
    static final int PORT = 8000;
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup) // Set boss & worker groups
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new StringDecoder());
                            p.addLast(new StringEncoder());
                            p.addLast(new ServerHandler());
                        }
                    });
            // Start the server.
            ChannelFuture f = b.bind(PORT).sync();
            System.out.println("Netty Server started.");
            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
@Sharable
class ServerHandler extends SimpleChannelInboundHandler<String> {
    static final List<Channel> channels = new ArrayList<Channel>();

    static final List<Game> games = new ArrayList<Game>();
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Client joined - " + ctx);
        channels.add(ctx.channel());

        int playerNum = channels.indexOf(ctx.channel());
        System.out.println(playerNum);

        Game game;

        if(playerNum%2==0) {
            game =new Game();
            games.add(game);
            game.setPlayer1(ctx.channel());

        }
        else {
            game =games.get(playerNum / 2);
            game.setPlayer2(ctx.channel());
        }
        games.set(playerNum/2,game);

        System.out.println("Game Num: " + playerNum/2);
        System.out.println("Player 1 " + games.get(playerNum / 2).getPlayer1());
        System.out.println("Player 2 " +games.get(playerNum / 2).getPlayer2());

    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Message received: " + msg);
        /*Game game=null;
        for (Game g:games) {
            if(g.inThisGame(ctx.channel()))
                game=g;
        }*/
        int playerNum = channels.indexOf(ctx.channel());
        Game game  = games.get(playerNum/2);
        Channel p1 = game.getPlayer1();
        Channel p2 = game.getPlayer2();
        String [] board;
        board = game.getBoard();
        String O = "O";
        String X = "X";
        boolean playerXTurn = game.isTurn();
        String[] str = msg.toString().split(",");

        if(msg.equals("Start")) {
            if (playerNum % 2 == 0)
                p1.writeAndFlush("Wait to another player , "+'\n'+"Player"+ X);
            else {
                String drawBoard = game.drawBoard();
                p1.writeAndFlush("Game started, your Turn \n" +drawBoard + '\n' +"Player"+ X);
                p2.writeAndFlush("Game started, \n" +drawBoard + '\n'+"Player"+ O);
                game.setTurn(false);
            }
        }
        else if (str[0].matches("[1-9]"))
        {
            if(game.emptyCell(Integer.parseInt(str[0])-1)){

            board[Integer.parseInt(str[0])-1] = str[1];
            if(!isWin(game.getBoard())&&!game.isDraw()){
                game.setBoard(board);
                if(playerXTurn) {
                    p1.writeAndFlush("your Turn Player"+ X +game.drawBoard()  + "\n"+ X);
                    p2.writeAndFlush(game.drawBoard()  + "\n"+ O);
                    game.setTurn(false);
                }
                else {
                    p2.writeAndFlush("your Turn  Player"+ O+game.drawBoard() + "\n"+  O);
                    p1.writeAndFlush(game.drawBoard()  + "\n"+ X);
                    game.setTurn(true);
                }
                //board[Integer.parseInt(str[0])-1] = str[1];
            }
            else
            {
                if(!game.isDraw()) {
                    if (!game.isTurn()) {
                        p1.writeAndFlush("You WON \n" + game.drawBoard());
                        p2.writeAndFlush("You Lose \n" + game.drawBoard());
                    } else {
                        p2.writeAndFlush("You WON \n" + game.drawBoard());
                        p1.writeAndFlush("You Lose \n" + game.drawBoard());
                    }
                }
                else{
                    p2.writeAndFlush("Draw \n" + game.drawBoard());
                    p1.writeAndFlush("Draw \n" + game.drawBoard());
                }

        }}
            else{
                    if(playerXTurn) {
                        p2.writeAndFlush("this place is full"+"\n"+ X);
                    }
                    else {
                        p1.writeAndFlush("this place is full"+"\n"+  O);
                    }
                }
        }
      /*  for (Channel c : channels) {
            c.writeAndFlush("Hello " + msg + '\n');

        }*/
    }
    public  boolean isWin(String[] board){
        int[][] lines = { {0, 1, 2}, {3, 4, 5},{6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6} };
        for (int i=0; i<8;i++){
            int a = lines[i][0];
            int b = lines[i][1];
            int c= lines[i][2];
            if(board[a].equals(board[b]) &&board[a].equals(board[c])&& !board[a].equals("-") )
            {
                return true;
            }
        }
        return false;

    }
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, String s) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("Closing connection for client - " + ctx);
        ctx.close();
    }
}