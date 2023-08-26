package com.hirshi001.game.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.github.czyzby.websocket.GwtWebSockets;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.hirshi001.buffer.bufferfactory.BufferFactory;
import com.hirshi001.buffer.bufferfactory.DefaultBufferFactory;
import com.hirshi001.game.MainGame;
import com.hirshi001.game.Network;
import com.hirshi001.gwtnetworking.GWTNetworkingFactory;
import com.hirshi001.gwtnetworking.SecureGWTNetworkingFactory;
import com.hirshi001.gwtrestapi.GWTRestFutureFactory;
import com.hirshi001.networking.network.NetworkFactory;
import com.hirshi001.restapi.RestAPI;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
        @Override
        public GwtApplicationConfiguration getConfig () {
            // Resizable application, uses available space in browser with no padding:
            GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(true);
            cfg.padVertical = 0;
            cfg.padHorizontal = 0;
            return cfg;
            // If you want a fixed size application, comment out the above resizable section,
            // and uncomment below:
            //return new GwtApplicationConfiguration(640, 480);
        }

        @Override
        public ApplicationListener createApplicationListener () {
            GwtWebSockets.initiate();
            RestAPI.setFactory(new GWTRestFutureFactory());
            BufferFactory bufferFactory = new DefaultBufferFactory();
            NetworkFactory networkFactory = new SecureGWTNetworkingFactory();
            String host = "game.hrishislife.com";
            int port = Network.HTML_PORT;

            return new MainGame(networkFactory, bufferFactory, host, port, isMobileDevice());
        }
}
