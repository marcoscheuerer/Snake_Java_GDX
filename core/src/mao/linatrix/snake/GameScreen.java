package mao.linatrix.snake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends ScreenAdapter {

	private SpriteBatch batch;
	private Texture snakehead;
	private Texture snakebody;
	private Texture apple;
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
	
	@Override
	public void show() {
		batch = new SpriteBatch();
		snakehead = new Texture(Gdx.files.internal("snakehead.png"));
		snakebody = new Texture(Gdx.files.internal("snakebody.png"));
		apple = new Texture(Gdx.files.internal("apple.png"));
	}
	
	@Override
	public void render(float delta) {
		queryInput();
		
		timer -= delta;
		if (timer <= 0) {
			timer = MOVE_TIME;
			//snakeX += SNAKE_MOVEMENT;
			moveSnake();
			checkForOutOfBounds();
			updateBodyPartsPosition();
			checkAppleCollision();
		}
		
		checkAndPlaceApple();
		
		clearScreen();
		draw();
	}
	
	private void clearScreen() {
		Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
	
	private void draw() {
		batch.begin();
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
		
		if (lPressed) snakeDirection = LEFT;
		if (rPressed) snakeDirection = RIGHT;
		if (uPressed) snakeDirection = UP;
		if (dPressed) snakeDirection = DOWN;
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
	
}
