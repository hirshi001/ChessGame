package com.hirshi001.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hirshi001.game.packets.AcceptChallengePacket;
import com.hirshi001.game.packets.ChallengePacket;

public class MenuScreen extends GameScreen {


    public static java.util.List<String> challenges;
    public static boolean challengeListChanged = true;

    public static java.util.List<String> players;
    public static boolean playerListChanged = true;


    Stage stage;
    List<String> playerList;
    TextButton challengeButton;

    VerticalGroup challengesList;




    public MenuScreen(MainGame game) {
        super(game);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        ScreenUtils.clear(Color.GRAY);
        checkPlayerList();
        checkChallengeList();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        super.show();
        playerListChanged = true;
        challengeListChanged = true;

        stage = new Stage(new ScreenViewport());

        Table listPlayerTable = new Table();
        listPlayerTable.left();
        listPlayerTable.setFillParent(true);


        challengeButton = new TextButton("No Player Selected", game.uiSkin);
        challengeButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                sendChallenge(playerList.getSelected());
              }
        });
        listPlayerTable.add(challengeButton).expandX();

        listPlayerTable.row();

        playerList = new List<>(game.uiSkin);
        playerList.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                String name = playerList.getSelected();
                challengeButton.setText("Challenge "+name);

            }
        });
        listPlayerTable.add(playerList);
        checkPlayerList();

        stage.addActor(listPlayerTable);


        Label nameLabel = new Label("Your name: " + game.name, game.uiSkin);
        nameLabel.setPosition(0, 0);
        stage.addActor(nameLabel);



        Table challengesTable = new Table();
        challengesTable.setFillParent(true);
        challengesTable.right().top();
        challengesTable.add(new Label("Challenges", game.uiSkin));
        challengesTable.row();

        challengesList = new VerticalGroup();
        challengesTable.add(challengesList);

        stage.addActor(challengesTable);

        Gdx.input.setInputProcessor(stage);
    }

    private void checkPlayerList(){
        if(playerListChanged){
            if(players.size()==0){
                playerList.setItems("No players");
            } else {
                playerList.setItems(players.toArray(new String[0]));
            }
            if(playerList.getSelected()==null){
                challengeButton.setText("No Player Selected");
            }else{
                challengeButton.setText("Challenge "+playerList.getSelected());
            }
            playerListChanged = false;
        }
    }

    private void checkChallengeList(){
        if(challengeListChanged){
            System.out.println("Updating challenge list");
            challengesList.clear();
            for(String s : challenges){
                challengesList.addActor(createChallengeActor(s));
            }
            challengeListChanged = false;
            challengesList.invalidateHierarchy();
        }
    }

    private Actor createChallengeActor(String name){
        final Table table = new Table();
        table.add(new Label(name, game.uiSkin));
        table.row();
        TextButton acceptButton = new TextButton("Accept", game.uiSkin);
        acceptButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                acceptChallenge(name);
            }
        });

        TextButton declineButton = new TextButton("Decline", game.uiSkin);
        declineButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                declineChallenge(name, table);
            }
        });
        table.add(acceptButton);
        table.add(declineButton);

        return table;
    }

    private void acceptChallenge(String name){
        Gdx.app.log("MenuScreen", "Accepting challenge from "+name);
        game.client.getChannel().sendTCP(new AcceptChallengePacket(name), null).perform();
    }

    private void declineChallenge(String name, Actor actor){
        Gdx.app.log("MenuScreen", "Declining challenge from "+name);
        actor.remove();
    }

    private void sendChallenge(String name){
        Gdx.app.log("MenuScreen", "Challenge Button Clicked");
        MainGame.INSTANCE().client.getChannel().sendTCP(new ChallengePacket(playerList.getSelected()), null).perform();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
