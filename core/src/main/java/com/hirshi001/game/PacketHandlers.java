package com.hirshi001.game;

import com.badlogic.gdx.Gdx;
import com.github.bhlangonijr.chesslib.Board;
import com.hirshi001.game.packets.*;
import com.hirshi001.networking.packethandlercontext.PacketHandlerContext;

import java.util.List;

public class PacketHandlers {

    public static void handlePlayerPacket(PacketHandlerContext<PlayerPacket> ctx) {

        List<String> players = MenuScreen.players;
        if (ctx.packet.add && !players.contains(ctx.packet.name)) {
            players.add(ctx.packet.name);
        } else {
            players.remove(ctx.packet.name);
        }
        MenuScreen.playerListChanged = true;

    }

    public static void handleMovePacket(PacketHandlerContext<MovePacket> ctx) {
        Board board = ChessGame.board;
        if(board!=null) board.doMove(ctx.packet.move);
    }

    public static void handleChallengePacket(PacketHandlerContext<ChallengePacket> ctx) {
        if(MenuScreen.challenges.contains(ctx.packet.name)) return;
        MenuScreen.challenges.add(ctx.packet.name);
        MenuScreen.challengeListChanged = true;
    }
    public static void handleStartGamePacket(PacketHandlerContext<StartGamePacket> ctx){
        Gdx.app.postRunnable( ()->{
            ChessGame.side = ctx.packet.side;
            MainGame game = MainGame.INSTANCE();
            game.setScreen(new ChessGame(game, ctx.packet.name));
        });
    }

    public static void handleGameOverPacket(PacketHandlerContext<GameOverPacket> ctx){
        Gdx.app.postRunnable( ()->{
            if(ctx.packet.draw){
                ChessGame.state = ChessGame.State.DRAW;
            }
            else if(ctx.packet.win){
                ChessGame.state = ChessGame.State.WIN;
            }
            else{
                ChessGame.state = ChessGame.State.LOSE;
            }
        });
    }


}
