package com.hirshi001.game.keybaord;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class Key extends TextButton {

    private final int keyCode;
    KeyListener listener;
    public Key(String text, Skin skin, int keyCode) {
        super(text, skin);
        this.keyCode = keyCode;
        addListener(new ClickListener(){
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if(listener!=null) listener.keyDown(keyCode);
            }
        });
    }


    public void setKeyListener(KeyListener listener){
        this.listener = listener;
    }
}
