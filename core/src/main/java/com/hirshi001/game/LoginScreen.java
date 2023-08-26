package com.hirshi001.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hirshi001.game.keybaord.KeyListener;
import com.hirshi001.game.keybaord.KeyboardActor;
import com.hirshi001.game.packets.LoginPacket;
import com.hirshi001.networking.packethandlercontext.PacketHandlerContext;
import com.hirshi001.networking.util.defaultpackets.primitivepackets.BooleanPacket;

public class LoginScreen extends GameScreen{

    Stage stage;
    Table table;

    TextField nameField;

    public LoginScreen(MainGame game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        ScreenUtils.clear(Color.GRAY);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {
        super.show();
        stage = new Stage(new ScreenViewport());
        table = new Table();
        table.setFillParent(true);

        Label nameLabel = new Label("Enter Name:", game.uiSkin);
        table.add(nameLabel).padRight(10F);

        nameField = new TextField("", game.uiSkin);
        nameField.setMessageText("Ex: John Doe");
        table.add(nameField);

        TextButton loginButton = new TextButton("Login", game.uiSkin);
        loginButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                String name = nameField.getText().trim();
                Gdx.app.log("LoginScreen", "Login button clicked. Name: "+name);

                game.client.getChannel().sendTCPWithResponse(new LoginPacket(name), null, 1000).
                    map(ctx -> (PacketHandlerContext<BooleanPacket>) (Object)ctx).
                    then(ctx -> {
                        if(ctx.packet.value){
                            Gdx.app.log("LoginScreen", "Login successful");
                            game.name = name;
                            Gdx.app.postRunnable( ()-> game.setScreen(new MenuScreen(game)));
                        }else{
                            throw new RuntimeException("Login failed for unknown reason");
                        }
                    }).onFailure(cause -> Gdx.app.debug("LoginScreen", "Login failed", cause)).performAsync();
            }
        });

        table.row().padTop(5F);
        table.add(new Actor());
        table.add(loginButton).fillX();


        stage.addActor(table);

        if(MainGame.INSTANCE().isMobile) {
            Table keyboardTable = new Table().bottom();
            keyboardTable.setFillParent(true);
            KeyboardActor keyboardActor = new KeyboardActor();
            keyboardActor.bottom();
            keyboardTable.add(keyboardActor).growX().bottom().pad(20F).padBottom(100F);
            keyboardActor.setKeyListener(new KeyListener() {
                @Override
                public void keyDown(int keyCode) {
                    if (keyCode == Input.Keys.BACKSPACE) {
                        String text = nameField.getText();
                        if (text.length() > 0) nameField.setText(text.substring(0, text.length() - 1));
                    } else if (keyCode == Input.Keys.SPACE) {
                        nameField.setText(nameField.getText() + " ");
                    } else if (keyCode == Input.Keys.ENTER) {
                        loginButton.fire(new InputEvent());
                    } else {
                        String c = Input.Keys.toString(keyCode);
                        if (c.length() == 1) {
                            nameField.setText(nameField.getText() + c);
                        }
                    }
                    nameField.invalidate();
                }
            });

            stage.addActor(keyboardTable);
            // stage.setDebugAll(true);
        }

        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, true);
    }
}
