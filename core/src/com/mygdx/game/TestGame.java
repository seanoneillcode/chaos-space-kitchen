package com.mygdx.game;

import static com.badlogic.gdx.math.MathUtils.random;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class TestGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img, boxImage;

	public static final int WORLD_WIDTH = 480;
	public static final int WORLD_HEIGHT = 360;
	private static final float PLAYER_SPEED = 5000.0f;
	private float screenWidth;
	private float screenHeight;
	private OrthographicCamera camera;
	World world;
	Matrix4 debugMatrix;
	Box2DDebugRenderer debugRenderer;
	Body playerBody;
	Vector2 lastDirection;
	Vector2 inputVector;
	EntityFactory entityFactory;

	int numBoxes = 0;
	public static final int WORLD_BUFFER = 16;

	List<Body> boxes = new ArrayList<Body>();

	@Override
	public void create () {
		// camera
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();

		// world
		world = new World(new Vector2(0, 0), true);
		world.setContinuousPhysics(true);
		entityFactory = new EntityFactory();

		batch = new SpriteBatch();

		// images
		img = new Texture("player.png");
		boxImage = new Texture("box.png");


		// start game
		numBoxes = 5;
		resetLevel();
	}

	public void resize (int width, int height) {
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		camera = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT ); //* (screenHeight / screenWidth)
		Vector3 cameraPosition = camera.position.cpy();
		cameraPosition.x = WORLD_WIDTH / 2.0f;
		cameraPosition.y = WORLD_HEIGHT / 2.0f;
		camera.position.set(cameraPosition);
		camera.update();
		debugMatrix = new Matrix4(camera.combined);
		debugRenderer = new Box2DDebugRenderer();
		batch.setProjectionMatrix(camera.combined);
	}

	@Override
	public void render () {

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		handleInput();
		update();

		world.step(Gdx.graphics.getDeltaTime(), 6, 2);
		playerBody.setAwake(true);

		Gdx.gl.glClearColor(0.0f, 0.1f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();


		Vector2 pos = getDrawPlayerPos();
		batch.draw(img, pos.x, pos.y);

		for (Body box : boxes) {
			Vector2 boxPos = box.getPosition();
			batch.draw(boxImage, boxPos.x, boxPos.y);
		}

		batch.end();

		debugRenderer.render(world,  new Matrix4(camera.combined));
	}

	public void resetLevel() {
		lastDirection = new Vector2();
		inputVector = new Vector2();
		playerBody = entityFactory.createPlayer(world);
		boxes = new ArrayList<Body>();
		for (int i = 0; i < numBoxes; i++) {
			Vector2 randPos = new Vector2(
					random(WORLD_BUFFER,WORLD_WIDTH - WORLD_BUFFER),
					random(WORLD_BUFFER, WORLD_HEIGHT - WORLD_BUFFER));
			boxes.add(entityFactory.createBox(world, randPos));
		}
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}


	// UPDATE

	public void update() {
		Vector2 newPos = getPlayerPos();
		if (newPos.x < WORLD_BUFFER) {
			newPos.x = WORLD_BUFFER;
		}
		if (newPos.x > WORLD_WIDTH - WORLD_BUFFER) {
			newPos.x = WORLD_WIDTH - WORLD_BUFFER;
		}
		if (newPos.y < WORLD_BUFFER) {
			newPos.y = WORLD_BUFFER;
		}
		if (newPos.y > WORLD_HEIGHT - WORLD_BUFFER) {
			newPos.y = WORLD_HEIGHT - WORLD_BUFFER;
		}
		setPlayerPos(newPos);
	}

	// PLAYER

	private void setPlayerPos(Vector2 pos) {
		playerBody.setTransform(pos.cpy(), 0);
	}

	private void setPlayerPos(float x, float y) {
		setPlayerPos(new Vector2(x, y));
	}

	private Vector2 getPlayerPos() {
		return playerBody.getPosition().cpy();
	}

	private Vector2 getDrawPlayerPos() {
		return getPlayerPos();
	}

	// INPUT

	public void handleInput() {

		float actualSpeed = PLAYER_SPEED * Gdx.graphics.getDeltaTime();
		boolean isLeftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
		boolean isRightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
		boolean isUpPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
		boolean isDownPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);

		Vector2 pos = playerBody.getPosition();
		inputVector.x = 0;
		inputVector.y = 0;

		if (isLeftPressed) {
			inputVector.x = inputVector.x - 1;
			lastDirection = inputVector.cpy();
			playerBody.applyLinearImpulse(-actualSpeed, 0, pos.x, pos.y, true);
		}
		if (isRightPressed) {
			inputVector.x = inputVector.x + 1;
			lastDirection = inputVector.cpy();
			playerBody.applyLinearImpulse(actualSpeed, 0, pos.x, pos.y, true);
		}
		if (isUpPressed) {
			inputVector.y = inputVector.y - 1;
			lastDirection = inputVector.cpy();
			playerBody.applyLinearImpulse(0, actualSpeed, pos.x, pos.y, true);
		}
		if (isDownPressed) {
			inputVector.y = inputVector.y + 1;
			lastDirection = inputVector.cpy();
			playerBody.applyLinearImpulse(0, -actualSpeed, pos.x, pos.y, true);
		}


		if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			Gdx.app.exit();
		}
	}


}
