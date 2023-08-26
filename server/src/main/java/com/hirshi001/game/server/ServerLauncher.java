package com.hirshi001.game.server;

import com.hirshi001.buffer.bufferfactory.BufferFactory;
import com.hirshi001.buffer.bufferfactory.DefaultBufferFactory;
import com.hirshi001.game.Network;
import com.hirshi001.game.packets.*;
import com.hirshi001.javanetworking.JavaNetworkFactory;
import com.hirshi001.javarestapi.JavaRestFutureFactory;
import com.hirshi001.networking.network.NetworkFactory;
import com.hirshi001.networking.network.channel.Channel;
import com.hirshi001.networking.network.channel.ChannelInitializer;
import com.hirshi001.networking.network.channel.ChannelOption;
import com.hirshi001.networking.network.server.AbstractServerListener;
import com.hirshi001.networking.network.server.Server;
import com.hirshi001.networking.network.server.ServerListener;
import com.hirshi001.networking.network.server.ServerOption;
import com.hirshi001.networking.networkdata.DefaultNetworkData;
import com.hirshi001.networking.networkdata.NetworkData;
import com.hirshi001.networking.packetregistry.PacketRegistry;
import com.hirshi001.networking.packetregistrycontainer.PacketRegistryContainer;
import com.hirshi001.networking.packetregistrycontainer.SinglePacketRegistryContainer;
import com.hirshi001.restapi.RestAPI;
import com.hirshi001.restapi.ScheduledExec;
import com.hirshi001.websocketnetworkingserver.WebsocketServer;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Launches the server application. */
public class ServerLauncher{

    public static NetworkFactory javaNetworkFactory;
    public static BufferFactory bufferFactory;
    public static ScheduledExecutorService executorService;
    public static String KEYSTORE_PASSWORD;

    public static PacketRegistryContainer container;
    public static NetworkData networkData;
    public static Server javaServer, gwtServer;

    public static final Map<String, User> users = new HashMap<>();

    public static void main(String[] args) throws Exception {
        if(args.length==0) KEYSTORE_PASSWORD = "password";
        else KEYSTORE_PASSWORD = args[0];


        executorService = Executors.newScheduledThreadPool(5);
        RestAPI.setFactory(new JavaRestFutureFactory());
        javaNetworkFactory = new JavaNetworkFactory(executorService);

        bufferFactory = new DefaultBufferFactory();

        run();
    }

    public static void run() throws Exception {
        container = new SinglePacketRegistryContainer();
        networkData = new DefaultNetworkData(Network.ENCODER_DECODER, container);
        javaServer = javaNetworkFactory.createServer(networkData, bufferFactory, Network.JAVA_PORT);
        gwtServer = new WebsocketServer(RestAPI.getDefaultExecutor(), networkData, bufferFactory, Network.HTML_PORT); // networkFactory.createServer(networkData, bufferFactory, port);
        setSSL();
        startServer();
    }

    private static void setSSL(){
        try {
            final String password = KEYSTORE_PASSWORD;
            final char[] passwordChars = password.toCharArray();

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(Files.newInputStream(Paths.get("cert.jks")), passwordChars);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, passwordChars);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keyStore);


            SSLContext sslContext = null;
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            ((WebsocketServer)gwtServer).setWebsocketSocketServerFactory(new DefaultSSLWebSocketServerFactory(sslContext));
            System.out.println("SSL Enabled");
        } catch (Exception e) {
            System.err.println("Failed to set SSL");
            e.printStackTrace();
        }
    }

    public static void startServer() throws Exception {
        ServerListener serverListener = new AbstractServerListener() {
            @Override
            public void onClientConnect(Server server, Channel clientChannel) {
                System.out.println("Client connected: "+clientChannel);
            }

            @Override
            public void onClientDisconnect(Server server, Channel clientChannel) {
                super.onClientDisconnect(server, clientChannel);
                System.out.println("Client disconnected: "+clientChannel);
                if(clientChannel.getAttachment()!=null){
                    User user = (User) clientChannel.getAttachment();
                    System.out.println("User disconnected: "+user.username);
                    synchronized (users){
                        for(User u : users.values()){
                            u.channel.sendTCP(new PlayerPacket(user.username, false), null).perform();
                        }
                        users.remove(user.username);
                    }
                }
            }
        };

        PacketRegistry packetRegistry = networkData.getPacketRegistryContainer().getDefaultRegistry();
        packetRegistry.registerDefaultPrimitivePackets();

        packetRegistry
            .register(LoginPacket::new, PacketHandlers::handleLoginPacket, LoginPacket.class, 0)
            .register(PlayerPacket::new, null, PlayerPacket.class, 1)
            .register(MovePacket::new, PacketHandlers::handleMovePacket, MovePacket.class, 2)
            .register(ChallengePacket::new, PacketHandlers::handleChallengePacket, ChallengePacket.class, 3)
            .register(AcceptChallengePacket::new, PacketHandlers::handleAcceptChallengePacket, AcceptChallengePacket.class, 4)
            .register(StartGamePacket::new, null, StartGamePacket.class, 5)
            .register(GameOverPacket::new, null, GameOverPacket.class, 6)
            .register(ReloginPacket::new, PacketHandlers::handleReloginPacket, ReloginPacket.class, 7);

        ChannelInitializer channelInitializer = channel -> {
            channel.setChannelOption(ChannelOption.TCP_AUTO_FLUSH, true);
            channel.setChannelOption(ChannelOption.PACKET_TIMEOUT, TimeUnit.MINUTES.toNanos(5));
            // channel.setChannelOption(ChannelOption.PACKET_TIMEOUT, TimeUnit.MINUTES.toMillis(5));
        };


        javaServer.addServerListener(serverListener);
        javaServer.setChannelInitializer(channelInitializer);
        javaServer.startTCP().perform().get();
        System.out.println("Server started on port " + javaServer.getPort());

        gwtServer.addServerListener(serverListener);
        gwtServer.setChannelInitializer(channelInitializer);
        gwtServer.startTCP().perform().get();
        System.out.println("Websocket Server started on port " + gwtServer.getPort());


        while(true) {
            Thread.sleep(10);
            javaServer.checkTCPPackets();
            gwtServer.checkTCPPackets();

        }
    }
}
