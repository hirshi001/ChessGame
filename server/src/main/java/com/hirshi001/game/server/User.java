package com.hirshi001.game.server;

import com.github.bhlangonijr.chesslib.Side;
import com.hirshi001.networking.network.channel.Channel;

public class User {

    public String username;
    public Channel channel;
    public ChessGame chessGame;

    public User(String username, Channel channel) {
        this.username = username;
        this.channel = channel;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof User && ((User) obj).channel.equals(channel);
    }

    public User getOpponent(){
        if(chessGame==null) return null;
        if(chessGame.black==this) return chessGame.white;
        else return chessGame.black;
    }

    public Side getSide(){
        if(chessGame==null) return null;
        if(chessGame.black==this) return Side.BLACK;
        else return Side.WHITE;
    }
}
