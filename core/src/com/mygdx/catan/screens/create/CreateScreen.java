package com.mygdx.catan.screens.create;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.mygdx.catan.CatanGame;
import com.mygdx.catan.enums.ResourceKind;
import com.mygdx.catan.ui.CatanWindow;

public class CreateScreen implements Screen {

    private static final String TITLE = "CreateGame";

    private final CatanGame game;
    private final Screen parentScreen;
    private Stage stage;
    private Texture bg;


    // all values necessary to draw hexagons. Note that only length needs to be changed to change size of board
    private final int SIZE = 7;												// number of tiles at longest diagonal
    private final int LENGTH = 30;											// length of an edge of a tile
    private final int BASE = (int) Math.sqrt(Math.pow(LENGTH, 2) - Math.pow(LENGTH/2, 2)); // length of base of equilateral triangles within a tile
    private final int OFFX = BASE;											// offset on the x axis
    private final int OFFY = LENGTH + LENGTH/2;								// offset on the y axis

    private Random rd = new Random();

    private Pair<Integer,Integer>[] aHexPositions;
    private int[] aIntersectionPositions;
    private int[] aHexKindSetup;

    PolygonSprite poly;
    PolygonSpriteBatch polyBatch = new PolygonSpriteBatch(); // To assign at the beginning

    Texture aWaterTextureSolid;
    Texture aDesertTextureSolid;
    Texture aClayTextureSolid;
    Texture aForrestTextureSolid;
    Texture aStoneTextureSolid;

    public CreateScreen(CatanGame pGame, Screen parentScreen) {
        this.parentScreen = parentScreen;
        game = pGame;
    }

    @Override
    public void show() {
        bg = new Texture("BG.png");
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        // Create & add the window
        CatanWindow window = new CatanWindow(TITLE, game.skin);
        window.setWindowListener(() -> game.setScreen(parentScreen));
        window.debugAll();

        Table contentTable = new Table(game.skin);
        contentTable.padTop(window.getPadTop());
        contentTable.setFillParent(true);

        Table gameNameTable = new Table(game.skin);
        TextField gameName = new TextField("", game.skin);
        gameNameTable.defaults().expand().fill().padBottom(4f).padTop(20);
        gameNameTable.add(new Label("Game Name:", game.skin)).height(20).padTop(20);
        gameNameTable.add(gameName).height(20);
        gameNameTable.pack();

        ArrayList<String> mapNames = new ArrayList<>();
        mapNames.add("Map #1");
        mapNames.add("Map #2");

        Table mapTable = new Table(game.skin);
        mapTable.background(game.skin.newDrawable("background", Color.valueOf("eee9e9")));
        mapTable.add(new Label("Maps", game.skin)).padBottom(20);
        mapTable.row();
        mapTable.top();
        for (String mapName : mapNames){
            mapTable.add(new TextButton(mapName, game.skin)).height(30).width(420).center().padBottom(1);
            mapTable.row();
        }
        mapTable.add(new TextButton("Random", game.skin)).height(35).width(420).center().padTop(10);
        mapTable.pack();

        Table previewTable = new Table(game.skin);
        previewTable.background(game.skin.newDrawable("background", Color.valueOf("eee9e9")));
        previewTable.top();
        previewTable.add(new Label("Preview", game.skin));
        previewTable.row();
        previewTable.pack();

        TextButton create = new TextButton("Create", game.skin);

        contentTable.top().left();
        contentTable.add(gameNameTable).colspan(2);
        contentTable.row();
        contentTable.add(mapTable).width(425).expandX().height(400).pad(20);
        contentTable.add(previewTable).width(425).expandX().height(400).pad(20);
        contentTable.row();
        contentTable.add(create).height(50).width(400).colspan(2);


        // initialize hex position coordinates, where x=(aHexPositions[i].getLeft()) and y=(aHexPositions[i].getRight())
        // the coordinates describe the offset from the center.
        aHexPositions = new Pair[37];
        aHexKindSetup = new int[37];
        int index = 0;
        int half = SIZE / 2;

        for (int row = 0; row < SIZE; row++) {
            int cols = SIZE - java.lang.Math.abs(row - half);

            for (int col = 0; col < cols; col++) {
                int x = -cols + 2 * col + 1;
                int y = (row - half);
                aHexKindSetup[index] = rd.nextInt(ResourceKind.values().length);
                aHexPositions[index++] = new Pair(x,y);
            }
        }

        // Creating the color filling for hexagons
        aWaterTextureSolid = setupTextureSolid(Color.BLUE);
        aDesertTextureSolid = setupTextureSolid(Color.YELLOW);
        aClayTextureSolid = setupTextureSolid(Color.RED);
        aForrestTextureSolid = setupTextureSolid(Color.GREEN);
        aStoneTextureSolid = setupTextureSolid(Color.GRAY);

        contentTable.pack();
        window.row().fill().expand();
        contentTable.setSize(900,600);
        window.add(contentTable);
        window.setSize(950,600);
        stage.addActor(window);
    }

    private Texture setupTextureSolid(Color color) {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(color); // DE is red, AD is green and BE is blue.
        pix.fill();
        return new Texture(pix);
    }



    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.end();

        stage.act(delta);
        stage.draw();

        // sets center of board
        int xCenter = 82*Gdx.graphics.getWidth() / 120;
        int yCenter = 49*Gdx.graphics.getHeight() / 90;

        int offsetX, offsetY;

        // draws hexagons according to coordinates stored in aHexPositions and hex kinds stored in aHexKindSetup
        for(int i = 0; i < aHexPositions.length; i++) {
            offsetX = (aHexPositions[i].getLeft());
            offsetY = (aHexPositions[i].getRight());
            drawHexagon(xCenter + (offsetX * OFFX), yCenter + (offsetY * OFFY), LENGTH, BASE, ResourceKind.values()[aHexKindSetup[i]], delta);
        }

    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);
        game.batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        bg.dispose();
        stage.dispose();
    }

    /**
     * Draws a hexagon according to given position and length
     *
     * @param xPos x position of hexagon center
     * @param yPos y position of hexagon center
     * @param length length of the side of the hexagon
     * */
    private void drawHexagon(int xPos, int yPos, int length, int base, ResourceKind pResourceKind, float delta) {

        Texture aTexture = aWaterTextureSolid;

        // sets aTexture to relevant texture according to ResourceKind
        switch(pResourceKind) {
            case BRICK:
                aTexture = aClayTextureSolid;
                break;
            case GRAIN:
                aTexture = aDesertTextureSolid;
                break;
            case LUMBER:
                aTexture = aForrestTextureSolid;
                break;
            case ORE:
                aTexture = aStoneTextureSolid;
                break;
            case WOOL:
                aTexture = aClayTextureSolid;
                break;
            default:
                break;
        }

        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(aTexture),
                new float[] {      // Six vertices
                        xPos - base, yPos - length/2,        		// Vertex 0                4
                        xPos, yPos - length,       		    		// Vertex 1           5         3
                        xPos + base, yPos - length/2,		  	    // Vertex 2
                        xPos + base, yPos + length/2,    	   	    // Vertex 3           0         2
                        xPos, yPos + length,        				// Vertex 4                1
                        xPos - base, yPos + length/2				// Vertex 5
                }, new short[] {
                0, 1, 4,         // Sets up triangulation according to vertices above
                0, 4, 5,
                1, 2, 3,
                1, 3, 4
        });
        poly = new PolygonSprite(polyReg);
        poly.setOrigin(100, 100);
        polyBatch = new PolygonSpriteBatch();

        polyBatch.begin();
        poly.draw(polyBatch);
        polyBatch.end();

    }
}


class Pair<L,R> {

    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() { return left; }
    public R getRight() { return right; }

    @Override
    public int hashCode() { return left.hashCode() ^ right.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) return false;
        Pair pairo = (Pair) o;
        return this.left.equals(pairo.getLeft()) &&
                this.right.equals(pairo.getRight());
    }

}

