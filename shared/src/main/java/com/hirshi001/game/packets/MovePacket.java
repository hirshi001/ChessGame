package com.hirshi001.game.packets;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.networking.packet.Packet;

public class MovePacket extends Packet {

    public Move move;

    public MovePacket(){

    }

    public MovePacket(Move move){
        this.move = move;
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        out.writeByte(move.getFrom().ordinal());
        out.writeByte(move.getTo().ordinal());
        out.writeByte(move.getPromotion().ordinal());

    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        Square from = Square.values()[in.readByte()];
        Square to = Square.values()[in.readByte()];
        Piece promotion = Piece.values()[in.readByte()];
        move = new Move(from, to, promotion);
    }
}
