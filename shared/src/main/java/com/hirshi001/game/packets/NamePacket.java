package com.hirshi001.game.packets;

import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.buffer.util.ByteBufUtil;
import com.hirshi001.networking.packet.Packet;

public abstract class NamePacket extends Packet {

    public String name;

    public NamePacket() {
        super();
    }

    public NamePacket(String name){
        super();
        this.name = name;
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        ByteBufUtil.writeStringToBuf(name, out);
    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        name = ByteBufUtil.readStringFromBuf(in);
    }
}
