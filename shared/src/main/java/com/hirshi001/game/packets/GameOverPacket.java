package com.hirshi001.game.packets;

import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.networking.packet.Packet;
import com.hirshi001.networking.util.BooleanCompression;

public class GameOverPacket extends Packet {

    public boolean win, draw;

    public GameOverPacket() {
        super();
    }

    public GameOverPacket(boolean win, boolean draw) {
        this.win = win;
        this.draw = draw;
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        out.writeByte(BooleanCompression.compressBooleans(win, draw));
    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        byte b = in.readByte();
        win = BooleanCompression.getBoolean(b, 0);
        draw = BooleanCompression.getBoolean(b, 1);
    }
}
