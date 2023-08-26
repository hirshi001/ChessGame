package com.hirshi001.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.hirshi001.buffer.bufferfactory.BufferFactory;
import com.hirshi001.game.packets.*;
import com.hirshi001.networking.network.NetworkFactory;
import com.hirshi001.networking.network.channel.AbstractChannelListener;
import com.hirshi001.networking.network.channel.Channel;
import com.hirshi001.networking.network.channel.ChannelOption;
import com.hirshi001.networking.network.client.Client;
import com.hirshi001.networking.network.client.ClientOption;
import com.hirshi001.networking.networkdata.DefaultNetworkData;
import com.hirshi001.networking.networkdata.NetworkData;
import com.hirshi001.networking.packetregistry.PacketRegistry;
import com.hirshi001.networking.packetregistrycontainer.PacketRegistryContainer;
import com.hirshi001.networking.packetregistrycontainer.SinglePacketRegistryContainer;

import java.io.IOException;
import java.util.ArrayList;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MainGame extends Game {

    public static MainGame INSTANCE(){
        return (MainGame) Gdx.app.getApplicationListener();
    }

    public Skin uiSkin;
    public Client client;
    public NetworkFactory factory;
    public BufferFactory bufferFactory;
    public NetworkData networkData;
    public String host;
    public int port;
    public PacketRegistryContainer container;

    public String name;

    public boolean isMobile;


    public MainGame(NetworkFactory factory, BufferFactory bufferFactory, String host, int port) {
        this(factory, bufferFactory, host, port, false);
    }

    public MainGame(NetworkFactory factory, BufferFactory bufferFactory, String host, int port, boolean isMobile) {
        this.factory = factory;
        this.bufferFactory = bufferFactory;
        this.host = host;
        this.port = port;
        this.isMobile = isMobile;
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Gdx.app.LOG_DEBUG);
        MenuScreen.players = new ArrayList<>();
        MenuScreen.challenges = new ArrayList<>();
        try {
            setClient();
        }catch (Exception e){
            e.printStackTrace();
        }
        setScreen(new LoadScreen(this));
    }

    private void setClient() throws IOException {
        container = new SinglePacketRegistryContainer();
        networkData = new DefaultNetworkData(Network.ENCODER_DECODER, container);
        client = factory.createClient(networkData, bufferFactory, host, port);
        client.setChannelInitializer(channel -> {
            channel.setChannelOption(ChannelOption.TCP_AUTO_FLUSH, true);
            channel.setChannelOption(ChannelOption.TCP_NODELAY, true);
        });

        client.setClientOption(ClientOption.TCP_PACKET_CHECK_INTERVAL, 100);
        client.setClientOption(ClientOption.UDP_PACKET_CHECK_INTERVAL, 100);
        client.addClientListeners(new AbstractChannelListener() {
            @Override
            public void onTCPConnect(Channel channel) {
                super.onTCPConnect(channel);
                Gdx.app.debug("MainGame", "Connected to server");
            }

            @Override
            public void onTCPDisconnect(Channel channel) {
                super.onTCPDisconnect(channel);
                Gdx.app.debug("MainGame", "Disconnected from server");
            }
        });


        PacketRegistry packetRegistry = networkData.getPacketRegistryContainer().getDefaultRegistry();
        packetRegistry.registerDefaultPrimitivePackets();
        packetRegistry
            .register(LoginPacket::new, null, LoginPacket.class, 0)
            .register(PlayerPacket::new, PacketHandlers::handlePlayerPacket, PlayerPacket.class, 1)
            .register(MovePacket::new, PacketHandlers::handleMovePacket, MovePacket.class, 2)
            .register(ChallengePacket::new, PacketHandlers::handleChallengePacket, ChallengePacket.class, 3)
            .register(AcceptChallengePacket::new, null, AcceptChallengePacket.class, 4)
            .register(StartGamePacket::new, PacketHandlers::handleStartGamePacket, StartGamePacket.class, 5)
            .register(GameOverPacket::new, PacketHandlers::handleGameOverPacket, GameOverPacket.class, 6)
            .register(ReloginPacket::new, null, ReloginPacket.class, 7);


    }

    @Override
    public void render() {
        try {
            super.render();
        }catch (Exception e){
            Gdx.app.log("MainGame", "Error in render", e);
            Gdx.app.exit();
        }
    }
}
