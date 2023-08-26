package com.hirshi001.game.packets;

import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.buffer.util.ByteBufUtil;
import com.hirshi001.networking.packet.Packet;

public class PlayerPacket extends Packet {

    public String name;
    public boolean add;

    public PlayerPacket() {
    }

    public PlayerPacket(String name, boolean add) {
        this.name = name;
        this.add = add;
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        ByteBufUtil.writeStringToBuf(name, out);
        out.writeBoolean(add);
    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        name = ByteBufUtil.readStringFromBuf(in);
        add = in.readBoolean();
    }
}
