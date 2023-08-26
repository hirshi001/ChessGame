package com.hirshi001.game;

import com.hirshi001.networking.packetdecoderencoder.PacketEncoderDecoder;
import com.hirshi001.networking.packetdecoderencoder.SimplePacketEncoderDecoder;

public class Network {

    public static final PacketEncoderDecoder ENCODER_DECODER = new SimplePacketEncoderDecoder();
    public static final int JAVA_PORT = 4000;
    public static final int HTML_PORT = 4001;
}
