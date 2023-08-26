package com.hirshi001.game;

import com.badlogic.gdx.ScreenAdapter;

public abstract class GameScreen extends ScreenAdapter {

    public MainGame game;

    public GameScreen(MainGame game) {
        this.game = game;
    }
}
