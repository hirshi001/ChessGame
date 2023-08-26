package com.hirshi001.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hirshi001.networking.network.channel.ChannelOption;
import com.hirshi001.restapi.RestAPI;

import java.util.concurrent.TimeUnit;

/** First screen of the application. Displayed after the application is created. */
public class LoadScreen extends GameScreen {

    private AssetManager assetManager;
    private Label label;
    private Stage stage;
    public LoadScreen(MainGame game) {
        super(game);
    }

    @Override
    public void show() {
        // Prepare your screen here.
        assetManager = new AssetManager();
        SkinLoader.SkinParameter params = new SkinLoader.SkinParameter("uiskin.atlas");
        assetManager.load("uiskin.json", Skin.class, params);
        assetManager.finishLoading();
        game.uiSkin = assetManager.get("uiskin.json", Skin.class);

        stage = new Stage(new ScreenViewport());
        Table table = new Table();
        table.setFillParent(true);
        label = new Label("Connecting...", game.uiSkin);
        table.add(label).center();
        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);

        tryConnect();
    }

    private void tryConnect(){
        game.client.startTCP().onFailure(throwable -> {
            Gdx.app.debug("Client Start TCP", "Could not connect", throwable);
            // game.client.getExecutor().run(this::tryConnect, 1, TimeUnit.SECONDS);
        }).performAsync();
    }

    float dtime=0;

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        ScreenUtils.clear(Color.GRAY);

        dtime+=delta*2F;
        int portion = (int) (dtime) % 4;
        StringBuilder dots = new StringBuilder();
        for(int i = 0; i < portion; i++){
            dots.append(".");
        }
        label.setText("Connecting"+dots.toString());

        stage.act(delta);
        stage.draw();



        if (game.client != null && game.client.getChannel()!=null && game.client.isOpen()) {
            game.setScreen(new LoginScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        // This screen is never resized, but we show it anyway.
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }
}
