package com.hirshi001.game.packets;

import com.github.bhlangonijr.chesslib.Side;
import com.hirshi001.buffer.buffers.ByteBuffer;

public class StartGamePacket extends NamePacket {


    public Side side;
    public StartGamePacket() {
        super();
    }

    public StartGamePacket(Side side, String name){
        super(name);
        this.side = side;
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        if(side==Side.WHITE) out.writeByte(0);
        else out.writeByte(1);
    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        if(in.readByte()==0) side = Side.WHITE;
        else side = Side.BLACK;
    }
}
