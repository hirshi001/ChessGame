package com.hirshi001.game.server;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import com.hirshi001.game.packets.*;
import com.hirshi001.networking.packethandlercontext.PacketHandlerContext;
import com.hirshi001.networking.util.defaultpackets.primitivepackets.BooleanPacket;

public class PacketHandlers {

    public static void handleLoginPacket(PacketHandlerContext<LoginPacket> ctx) {

        String name = ctx.packet.name.trim();
        if (name.isEmpty()) {
            System.out.println("Attempted to login with empty username");
            ctx.channel.sendTCP(new BooleanPacket(true).setResponsePacket(ctx.packet), null).perform();
            return;
        }

        synchronized (ServerLauncher.users) {
            if (ServerLauncher.users.containsKey(name)) {
                System.out.println("Failed to login user " + name + " because the name is already taken.");
                ctx.channel.sendTCP(new BooleanPacket(false).setResponsePacket(ctx.packet), null).perform();
            } else {
                System.out.println("Logging in user: " + ctx.packet.name);
                User user = new User(name, ctx.channel);
                ctx.channel.attach(user);
                ServerLauncher.users.put(name, user);
                ctx.channel.sendTCP(new BooleanPacket(true).setResponsePacket(ctx.packet), null).perform();
                synchronized (ServerLauncher.users) {
                    ServerLauncher.users.forEach((s, user1) -> {
                        if (!user1.equals(user)) {
                            user1.channel.sendTCP(new PlayerPacket(user.username, true), null).perform();
                            user.channel.sendTCP(new PlayerPacket(s, true), null).perform();
                        }
                    });
                }
            }
        }
    }


    public static void handleMovePacket(PacketHandlerContext<MovePacket> ctx){
        User user = (User) ctx.channel.getAttachment();
        if(user.chessGame!=null){
            Side currentSide = user.chessGame.board.getSideToMove();
            if(currentSide!=user.getSide()) return;

            Move move = ctx.packet.move;
            Board board = user.chessGame.board;

            if(board.isMoveLegal(move, true) && board.legalMoves().contains(move)){
                board.doMove(move);
                ctx.channel.sendTCP(ctx.packet, null).perform();
                user.getOpponent().channel.sendTCP(ctx.packet, null).perform();

                if(board.isMated()){
                    user.channel.sendTCP(new GameOverPacket(false, false), null).perform();
                    user.getOpponent().channel.sendTCP(new GameOverPacket(true, false), null).perform();
                }
                else if(board.isDraw()) {
                    user.channel.sendTCP(new GameOverPacket(false, true), null).perform();
                    user.getOpponent().channel.sendTCP(new GameOverPacket(false, true), null).perform();
                }
            }
        }
    }

    public static void handleChallengePacket(PacketHandlerContext<ChallengePacket> ctx){
        User user = (User) ctx.channel.getAttachment();
        if(user==null || user.chessGame!=null) return;

        String name = ctx.packet.name;
        User other = ServerLauncher.users.get(name);
        if(other==null || other.chessGame!=null) return;

        other.channel.sendTCP(new ChallengePacket(user.username), null).perform();
    }

    public static void handleAcceptChallengePacket(PacketHandlerContext<AcceptChallengePacket> ctx){
        User user = (User) ctx.channel.getAttachment();
        if(user==null || user.chessGame!=null) return;

        String name = ctx.packet.name;
        User other = ServerLauncher.users.get(name);
        if(other==null || other.chessGame!=null) return;

        new ChessGame(user, other);
        user.channel.sendTCP(new StartGamePacket(Side.WHITE, other.username), null).perform();
        other.channel.sendTCP(new StartGamePacket(Side.BLACK, user.username), null).perform();
        synchronized (ServerLauncher.users){
            ServerLauncher.users.forEach((s, user1) -> {
                if(!user1.equals(user) && !user1.equals(other)){
                    user1.channel.sendTCP(new PlayerPacket(user.username, false), null).perform();
                    user1.channel.sendTCP(new PlayerPacket(other.username, false), null).perform();
                }
            });
        }
    }

    public static void handleReloginPacket(PacketHandlerContext<ReloginPacket> ctx){
        User user = (User) ctx.channel.getAttachment();
        if(user==null) return;
        user.chessGame=null;

        synchronized (ServerLauncher.users) {
            ServerLauncher.users.forEach((s, user1) -> {
                if (!user1.equals(user)) {
                    user1.channel.sendTCP(new PlayerPacket(user.username, true), null).perform();
                    user.channel.sendTCP(new PlayerPacket(s, true), null).perform();
                }
            });
        }

    }
}
