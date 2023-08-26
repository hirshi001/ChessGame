package com.hirshi001.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;
import com.hirshi001.game.packets.MovePacket;
import com.hirshi001.game.packets.ReloginPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChessGame extends GameScreen{

    public enum State{
        PLAYING, WIN, LOSE, DRAW
    }

    public static Board board;
    public static Side side;
    public static State state;

    String opponent;
    Stage stage, endGameStage;

    Image boardImage;
    Label endGameLabel;
    Table endGameTable;
    Map<String, TextureRegion> nameToTexture = new HashMap<>();
    SpriteBatch batch = new SpriteBatch();
    ShapeRenderer shapeRenderer = new ShapeRenderer();
    ScreenViewport viewport = new ScreenViewport();
    Color highlightColor = new Color(0x00ff0077);

    private final Vector2 boardPos = new Vector2(0, 0);
    private final Vector2 boardSize = new Vector2(0, 0);

    Square selectedSquare = null;

    List<Disposable> toDispose = new ArrayList<>();

    public ChessGame(MainGame game, String opponent) {
        super(game);
        this.opponent = opponent;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        ScreenUtils.clear(Color.GRAY);
        stage.act(delta);
        stage.draw();

        boardImage.localToScreenCoordinates(boardPos.set(0, 0));
        boardImage.localToScreenCoordinates(boardSize.set(boardImage.getWidth(), boardImage.getHeight())).sub(boardPos);

        highlightHoveredSquare();
        highlightSelectedSquare();
        if(state==State.PLAYING) drawPossibleMoves();
        drawPieces();

        if(state!=State.PLAYING){
            Gdx.input.setInputProcessor(endGameStage);
            String text = "";
            if(state==State.WIN) text = "You Win!";
            else if(state==State.LOSE) text = "You Lose!";
            else if(state==State.DRAW) text = "Draw!";
            endGameLabel.setText(text);
            endGameTable.setVisible(true);

            endGameStage.act(delta);
            endGameStage.draw();
        }
    }

    private void drawPossibleMoves(){

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // drawPossibleMoves();
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.2f);
        if(selectedSquare!=null){
            Piece piece = board.getPiece(selectedSquare);
            if(piece!=Piece.NONE && piece.getPieceSide()==side){
                if(board.getSideToMove()!=side) {
                    board.doNullMove();
                }
                List<Move> legalMoves = board.legalMoves();
                for(Move move: legalMoves){
                    if(move.getFrom().equals(selectedSquare)){
                        Square to = move.getTo();
                        float dx = (to.getFile().ordinal()) * boardSize.x / 8 + boardSize.x / 16;
                        float dy = (to.getRank().ordinal()) * boardSize.y / 8 + boardSize.y / 16;
                        float y = boardPos.y + dy;
                        if(side==Side.WHITE) y = Gdx.graphics.getHeight()-y;
                        shapeRenderer.circle(boardPos.x + dx, y, boardSize.x / 32);
                    }
                }
                if(board.getSideToMove()!=side) {
                    board.undoMove();
                }
            }
        }
        shapeRenderer.end();
    }

    private void highlightHoveredSquare(){

        Vector2 square = getSquareFromScreenCoords(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        // System.out.println(square);
        if(square.x<0 || square.x>7 || square.y<0 || square.y>7) return;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if(side==Side.BLACK) {
            square.y = 7 - square.y;
        }
        shapeRenderer.setColor(highlightColor);
        shapeRenderer.rect(boardPos.x+(int)(square.x)*boardSize.x/8f, Gdx.graphics.getHeight()-(boardPos.y+(int)(square.y)*boardSize.y/8f), boardSize.x/8f, -boardSize.y/8f);

        shapeRenderer.end();
    }

    private void highlightSelectedSquare(){
        if(selectedSquare==null) return;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.ORANGE);
        int file = selectedSquare.getFile().ordinal();
        int rank = selectedSquare.getRank().ordinal();

        if(side==Side.BLACK) {
            rank = 7 - rank;
        }
        shapeRenderer.rect(boardPos.x+ file *boardSize.x/8f, Gdx.graphics.getHeight()-(boardPos.y+ rank *boardSize.y/8f), boardSize.x/8f, -boardSize.y/8f);
        shapeRenderer.end();
    }


    private Vector2 getSquareFromScreenCoords(Vector2 screenCoords){
        // screenCoords.y = Gdx.graphics.getHeight()-screenCoords.y;

        screenCoords.sub(boardPos).scl(8/boardSize.x, 8/boardSize.y);
        screenCoords.x = (int)screenCoords.x;
        screenCoords.y = (int)screenCoords.y;
        if(side==Side.BLACK){
            screenCoords.y = 7-screenCoords.y;
        }
        return screenCoords;
    }

    private void drawPieces(){

        float squareWidth = boardSize.x/8f;
        float squareHeight = boardSize.y/8f;


        batch.begin();

        for (Square square : Square.values()) {
            if (Square.NONE.equals(square)) continue;
            Piece piece = board.getPiece(square);
            if(Piece.NONE.equals(piece)) continue;

            int col = square.getFile().ordinal();
            int row = square.getRank().ordinal();

            if(side==Side.BLACK){
                row = 7-row;
            }

            TextureRegion texture = nameToTexture.get(piece.name());
            batch.draw(texture, boardPos.x+col*squareWidth, boardPos.y+boardSize.y-row*squareHeight, squareWidth, -squareHeight);
        }

        batch.end();

    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, true);
        viewport.update(width, height, false);
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        shapeRenderer.updateMatrices();

        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);

    }

    @Override
    public void show() {
        super.show();
        ChessGame.board = new Board();
        ChessGame.state = State.PLAYING;
        stage = new Stage(new ScreenViewport());

        Table table = new Table();
        table.left().top();
        table.setFillParent(true);
        Label opponentName = new Label("Opponent: "+opponent, game.uiSkin);
        table.add(opponentName).left().top();
        stage.addActor(table);

        Label userName = new Label("Name: "+MainGame.INSTANCE().name, game.uiSkin);
        table = new Table();
        table.left().bottom();
        table.setFillParent(true);
        table.add(userName).left().bottom();
        stage.addActor(table);

        createBoardTexture();
        Table boardTable = new Table();
        boardTable.setFillParent(true);
        boardTable.center();
        boardTable.add(boardImage).center().minSize(128F, 128F)
                .prefSize(new Value() {
                    @Override
                    public float get(Actor context) {
                        float pWidth = context.getParent().getWidth()*0.9F;
                        float pHeight = context.getParent().getHeight()*0.9F;
                        return Math.min(pWidth, pHeight);
                    }
                });
        stage.addActor(boardTable);

        createPieceTextures();


        Gdx.input.setInputProcessor(new InputAdapter(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector2 square = getSquareFromScreenCoords(new Vector2(screenX, screenY));
                if(square.x<0 || square.x>7 || square.y<0 || square.y>7) return false;
                Square justSelected = Square.encode(Rank.allRanks[(int)square.y], File.allFiles[(int)square.x]);

                if(selectedSquare==null || board.getPiece(selectedSquare).equals(Piece.NONE)){
                    selectedSquare = justSelected;
                    return true;
                }

                Move move = new Move(selectedSquare, justSelected);
                if (board.isMoveLegal(move, true)) {
                    MainGame.INSTANCE().client.getChannel().sendTCP(new MovePacket(move), null).perform();
                    // board.doMove(move);
                    selectedSquare = null;
                    return true;
                }else{
                    selectedSquare = justSelected;
                    return true;
                }
            };
        });


        endGameStage = new Stage(new ScreenViewport());
        endGameTable = new Table();
        endGameTable.setFillParent(true);
        endGameTable.center();

        endGameLabel = new Label("", game.uiSkin);
        endGameTable.add(endGameLabel).center().row();

        TextButton endGameButton = new TextButton("Back to Main Menu", game.uiSkin);
        endGameButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.client.getChannel().sendTCP(new ReloginPacket(), null).perform();
                game.setScreen(new MenuScreen(game));
            }
        });
        endGameTable.add(endGameButton).center();

        endGameTable.setVisible(false);
        endGameStage.addActor(endGameTable);
    }

    private void createBoardTexture(){
        Pixmap pixmap = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixmap.setColor(Color.BROWN);
        int compare = side==Side.WHITE ? 0 : 1;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                if((i+j)%2==compare){
                    pixmap.drawPixel(i, j);
                }
            }
        }

        boardImage = new Image(new Texture(pixmap));
        toDispose.add(pixmap);
    }

    private void createPieceTextures(){
        Piece[] pieces = Piece.values();
        for(Piece piece : pieces){
            if(piece==Piece.NONE) continue;
            String[] names = piece.name().split("_");
            String color = names[0].charAt(0)+names[0].substring(1).toLowerCase();
            String type = names[1].charAt(0)+names[1].substring(1).toLowerCase();
            String name = color+"_" + type + ".png";

            TextureRegion region = new TextureRegion(new Texture(name));
            nameToTexture.put(piece.name(), region);
            toDispose.add(region.getTexture());
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        endGameStage.dispose();
        shapeRenderer.dispose();
        batch.dispose();
        for(Disposable d : toDispose) d.dispose();

    }
}
