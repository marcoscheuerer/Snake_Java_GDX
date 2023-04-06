package mao.linatrix.snake;

import java.awt.Font;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {

	private SpriteBatch batch;
	private Texture snakehead;
	private Texture snakebody;
	private Texture apple;
	private Texture gras;
	private Array<BodyPart> bodyParts = new Array<BodyPart>();
	
	private static final float MOVE_TIME = 0.3F;
	private float timer = MOVE_TIME;
	
	private static final int SNAKE_MOVEMENT = 32;
	private int snakeX = 0, snakeY = 0;
	private int snakeXBeforeUpdate =0, snakeYBeforeUpdate = 0;
	
	private boolean appleAvailable = false;
	private int appleX, appleY;
	
	private static final int RIGHT = 0;
	private static final int LEFT = 1;
	private static final int UP = 2;
	private static final int DOWN = 3;
	private static final int ESC = 4;
	private int snakeDirection = RIGHT;
	
	private ShapeRenderer shapeRenderer;
	private static final int GRID_CELL = SNAKE_MOVEMENT;
	
	private boolean directionSet;
	private boolean hasHit = false;
	
	private enum STATE {
		PLAYING, GAME_OVER;
	}
	private STATE state = STATE.PLAYING;
	
	private BitmapFont bitmapFont;
	private GlyphLayout layout;
	
	private static final String GAME_OVER_TEXT = "Game Over... \n\n\nPress space to restart!\n\nPress escape to quit!"; 
	
	@Override
	public void show() {
		shapeRenderer = new ShapeRenderer();
		batch = new SpriteBatch();
		snakehead = new Texture(Gdx.files.internal("snakehead.png"));
		snakebody = new Texture(Gdx.files.internal("snakebody.png"));
		apple = new Texture(Gdx.files.internal("apple.png"));
		gras = new Texture(Gdx.files.internal("gras.png"));
		bitmapFont = new BitmapFont();
		layout = new GlyphLayout();
	}
	
	@Override
	public void render(float delta) {
		switch (state) {
			case PLAYING:
				queryInput();
				updateSnake(delta);
				checkAppleCollision();
				checkAndPlaceApple();
				break;
			case GAME_OVER:
				checkForRestart();
				break;
		}
		
		clearScreen();
		drawGrid();
		draw();
	}
	
	private void updateSnake(float delta) {
		//if (!hasHit) {
			timer -= delta;
			if (timer <= 0) {
				timer = MOVE_TIME;
				moveSnake();
				checkForOutOfBounds();
				updateBodyPartsPosition();
				checkSnakeBodyCollision();
				directionSet = false;
			}		
		//}
	}
	
	private void clearScreen() {
		Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
	
	private void draw() {
		batch.begin();
		
		if (state == STATE.GAME_OVER) {
			layout.setText(bitmapFont, GAME_OVER_TEXT);
			bitmapFont.setColor(Color.RED);
			bitmapFont.draw(batch, GAME_OVER_TEXT, (Gdx.graphics.getWidth() - layout.width) / 2, (Gdx.graphics.getHeight() + layout.height) / 2);
		}
		
		batch.draw(snakehead, snakeX, snakeY);
		
		for (BodyPart bodyPart : bodyParts) {
			bodyPart.draw(batch);
		}
		
		if (appleAvailable) {
			batch.draw(apple, appleX, appleY);
		}
				
		batch.end();
		
	}
	
	private void checkForOutOfBounds() {
		if (snakeX >= Gdx.graphics.getWidth()) {
			snakeX = 0;
		}
		
		if (snakeX < 0) {
			snakeX = Gdx.graphics.getWidth() - SNAKE_MOVEMENT;
		}
		
		if (snakeY >= Gdx.graphics.getHeight()) {
			snakeY = 0;
		}
		
		if (snakeY < 0) {
			snakeY = Gdx.graphics.getHeight() - SNAKE_MOVEMENT;
		}
		
	}
	
	private void moveSnake() {
		snakeXBeforeUpdate = snakeX;
		snakeYBeforeUpdate = snakeY;
		
		switch(snakeDirection) {
			case RIGHT:
				snakeX += SNAKE_MOVEMENT;
				return;
			case LEFT:
				snakeX -= SNAKE_MOVEMENT;
				return;
			case UP:
				snakeY += SNAKE_MOVEMENT;
				return;
			case DOWN:
				snakeY -= SNAKE_MOVEMENT;
				return;
			case ESC:
				return;
		}
		
	}
	
	private void queryInput() {
		boolean lPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
		boolean rPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
		boolean uPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
		boolean dPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);
		boolean escPressed = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);
		
		if (lPressed) updateDirection(LEFT);
		if (rPressed) updateDirection(RIGHT);
		if (uPressed) updateDirection(UP);
		if (dPressed) updateDirection(DOWN);
		if (escPressed) Gdx.app.exit();
	}
	
	private void checkAndPlaceApple() {
		if (!appleAvailable) {
			do {
				appleX = MathUtils.random(Gdx.graphics.getWidth() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
				appleY = MathUtils.random(Gdx.graphics.getHeight() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
				appleAvailable = true;
			} while (appleX == snakeX && appleY == snakeY);
		}
	}
	
	private void checkAppleCollision() {
		if (appleAvailable && appleX == snakeX && appleY == snakeY) {
			BodyPart bodyPart = new BodyPart(snakebody);
			bodyPart.updateBodyPosition(snakeX, snakeY);
			bodyParts.insert(0, bodyPart);
			appleAvailable = false;
		}
	}
	
	private class BodyPart {
		private int x, y;
		private Texture texture;
		
		public BodyPart(Texture texture) {
			this.texture = texture;
		}
		
		public void updateBodyPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public void draw(Batch batch) {
			if (!(x == snakeX && y == snakeY))
				batch.draw(texture, x, y);
		}
	}
	
	private void updateBodyPartsPosition() {
		if (bodyParts.size > 0) {
			BodyPart bodyPart = bodyParts.removeIndex(0);
			bodyPart.updateBodyPosition(snakeXBeforeUpdate, snakeYBeforeUpdate);
			bodyParts.add(bodyPart);
		}
		
	}
	
	private void drawGrid() {
		SpriteBatch grasBatch = new SpriteBatch();
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		
		grasBatch.begin();
		for (int x = 0; x < Gdx.graphics.getWidth(); x += GRID_CELL) {
			for (int y = 0; y < Gdx.graphics.getHeight(); y += GRID_CELL) {
				//shapeRenderer.rect(x, y, GRID_CELL, GRID_CELL);
				grasBatch.draw(gras, x, y);
			}
		}
		grasBatch.end();
		
		shapeRenderer.end();
	}
	
	private void updateIfNotOppositeDirection(int newSnakeDirection, int oppositeDirection) {
		if (snakeDirection != oppositeDirection || bodyParts.size == 0)
			snakeDirection = newSnakeDirection;
	}
	
	private void updateDirection(int newSnakeDirection) {
		if (!directionSet && snakeDirection != newSnakeDirection) {
			directionSet = true;
			switch(newSnakeDirection) {
				case LEFT:
					updateIfNotOppositeDirection(newSnakeDirection, RIGHT);
					break;
				case RIGHT:
					updateIfNotOppositeDirection(newSnakeDirection, LEFT);
					break;
				case UP:
					updateIfNotOppositeDirection(newSnakeDirection, DOWN);
					break;
				case DOWN:
					updateIfNotOppositeDirection(newSnakeDirection, UP);
					break;
			
			}
		}
	}
	
	private void checkSnakeBodyCollision() {
		for (BodyPart bodyPart : bodyParts) {
			if (bodyPart.x == snakeX && bodyPart.y == snakeY) {
//				hasHit = true;
				state = STATE.GAME_OVER;
			}
		}
	}
	
	private void checkForRestart() {
		if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
			doRestart();
		else if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
			System.exit(0);
	}
	
	private void doRestart() {
		state = STATE.PLAYING;
		bodyParts.clear();
		snakeDirection = RIGHT;
		directionSet = false;
		timer = MOVE_TIME;
		snakeX = 0;
		snakeY = 0;
		snakeXBeforeUpdate = 0;
		snakeYBeforeUpdate = 0;
		appleAvailable = false;
	}
	
}
