package io.netty.example;

import io.netty.channel.*;

import java.util.Arrays;

public class Game {
    public Channel player1 = null;
    public Channel player2= null;
    public String[] board = new String[9];
    public boolean turn=true; //p1

    public boolean isTurn() {
        return turn;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    public Channel getPlayer1() {
        return player1;
    }

    public void setPlayer1(Channel player1) {
        this.player1 = player1;
    }

    public Channel getPlayer2() {
        return player2;
    }

    public void setPlayer2(Channel player2) {
        this.player2 = player2;
        Arrays.fill(this.board, "-");
    }

    public boolean inThisGame(Channel player) {
        return this.player1 == player || this.player2 == player;
    }

    public String[] getBoard() {
        return board;
    }

    public void setBoard(String[] board) {
        this.board = board;
    }

    public String drawBoard() {
        //String mat = "1 2 3\n4 5 6\n7 8 9\n";
       String mat = "\n" + this.board[0] + " " + this.board[1] + " " + this.board[2]
                +"\n" +  this.board[3] + " " + this.board[4] + " " + this.board[5]
                +"\n" +  this.board[6] + " " + this.board[7] + " " + this.board[8]+"\n";
        return mat;
    }

    public boolean emptyCell(int i) {
        return this.board[i].equals("-");
    }

    public boolean isDraw() {
        return Arrays.stream(this.board).noneMatch(s -> s.equals("-"));
    }
}