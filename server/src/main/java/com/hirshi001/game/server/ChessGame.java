package com.hirshi001.game.server;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

public class ChessGame {

    public User white, black;
    public Board board;

    public ChessGame(User white, User black) {
        this.white = white;
        this.black = black;
        board = new Board();

        white.chessGame = this;
        black.chessGame = this;

    }

    public boolean makeMove(User user, Move move){
        boolean success = board.doMove(move, true);
        System.out.println("User: " + user.username + ", move: "+move.toString()+", success: "+success);
        return success;
    }

}
