package com.mygdx.game;

import static com.badlogic.gdx.math.MathUtils.random;

import java.util.ArrayList;
import java.util.Iterator;
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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class TestGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img, boxImage, targetImage;

	public static final int WORLD_WIDTH = 480;
	public static final int WORLD_HEIGHT = 360;
	private static final float PLAYER_SPEED = 10000.0f;
	private static final float PICKUP_START_COOLDOWN = 0.2f;
	private static final float THROW_COOLDOWN = 0.5f;
	public static final float TILE_SIZE = 32.0f;
	public static final float HALF_SIZE = 16.0f;
	public static final float LIGHT_DENSITY = 0.001f;
	public static final float HEAVY_DENSITY = 0.1f;

	private static final float BOX_FORCE = 6.0f;

	private float screenWidth;
	private float screenHeight;
	private OrthographicCamera camera;
	World world;
	Matrix4 debugMatrix;
	Box2DDebugRenderer debugRenderer;
	Body playerBody;
	Vector2 lastDirection;
	Vector2 inputVector;
	Vector2 pickupAreaSize;
	Vector2 pickupPos;
	Vector2 pickedBoxPostion;
	Vector2 thrownMove;
	EntityFactory entityFactory;

	Body pickedBox;
	Body thrownBox;

	List<Target> targets;

	float pickupCooldown;
	float throwCooldown;

	int numBoxes = 0;
	int score = 0;
	public static final int WORLD_BUFFER = 16;
	public static final int TARGET_WORLD_BUFFER = 24;

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
		targetImage = new Texture("target.png");

		// start game
		numBoxes = 5;
		score = 0;
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

		for (Target target : targets) {
			batch.draw(targetImage, target.rect.x, target.rect.y);
		}

		for (Body box : boxes) {
			Vector2 boxPos = box.getPosition();
			batch.draw(boxImage, boxPos.x, boxPos.y);
		}




		//batch.draw(boxImage, pickupPos.x, pickupPos.y);

		batch.end();
		//debugRenderer.render(world, new Matrix4(camera.combined));
	}

	public void resetLevel() {
		lastDirection = new Vector2();
		inputVector = new Vector2();
		pickupPos = new Vector2();
		thrownMove = new Vector2();
		playerBody = entityFactory.createPlayer(world);
		boxes = new ArrayList<Body>();
		targets = new ArrayList<Target>();
		pickupAreaSize = new Vector2(TILE_SIZE, TILE_SIZE);
		pickupCooldown = 0;
		throwCooldown = 0;
		pickedBox = null;
		thrownBox = null;
		for (int i = 0; i < numBoxes; i++) {
			Vector2 randPos = new Vector2(
					random(WORLD_BUFFER,WORLD_WIDTH - WORLD_BUFFER),
					random(WORLD_BUFFER, WORLD_HEIGHT - WORLD_BUFFER));
			boxes.add(entityFactory.createBox(world, randPos));
		}
		addTarget();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}


	// UPDATE

	public void update() {
		setPlayerPos(worldConstrainedPosition(getPlayerPos()));
		for (Body box : boxes) {
			box.setTransform(worldConstrainedPosition(box.getPosition().cpy()), 0);
		}
		pickupCooldown = pickupCooldown - Gdx.graphics.getDeltaTime();
		throwCooldown = throwCooldown - Gdx.graphics.getDeltaTime();
		if (pickedBox != null) {
			Vector2 offset = getPlayerPos().cpy().add(0, TILE_SIZE + 4);
			pickedBoxPostion = offset;
			pickedBox.setTransform(offset, 0);
		}
		if (thrownBox != null) {
			if (throwCooldown < 0) {
				thrownBox = null;
			} else {
				thrownMove = thrownMove.scl(0.9f);
				Vector2 pos = thrownBox.getPosition().cpy().add(thrownMove);
				thrownBox.setTransform(pos.x, pos.y, 0);
			}
		}

		for (Target target : targets) {

			Iterator<Body> iter = boxes.listIterator();
			while (iter.hasNext()) {
				Body thisBox = iter.next();
				Vector2 boxPos = thisBox.getPosition();
				Rectangle boxRect = new Rectangle(boxPos.x, boxPos.y, TILE_SIZE, TILE_SIZE);

				// TODO ALSO CHECK IF TYPE IS CORRECT
				if (boxRect.overlaps(target.rect)) {
					iter.remove();
					world.destroyBody(thisBox);
					score = score + 1;
				}
			}
		}
	}

	public Vector2 worldConstrainedPosition(Vector2 in) {
		Vector2 out = in.cpy();
		if (out.x < WORLD_BUFFER) {
			out.x = WORLD_BUFFER;
		}
		if (out.x > WORLD_WIDTH - WORLD_BUFFER) {
			out.x = WORLD_WIDTH - WORLD_BUFFER;
		}
		if (out.y < WORLD_BUFFER) {
			out.y = WORLD_BUFFER;
		}
		if (out.y > WORLD_HEIGHT - WORLD_BUFFER) {
			out.y = WORLD_HEIGHT - WORLD_BUFFER;
		}
		return out;
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
			inputVector.y = inputVector.y + 1;
			lastDirection = inputVector.cpy();
			playerBody.applyLinearImpulse(0, actualSpeed, pos.x, pos.y, true);
		}
		if (isDownPressed) {
			inputVector.y = inputVector.y - 1;
			lastDirection = inputVector.cpy();
			playerBody.applyLinearImpulse(0, -actualSpeed, pos.x, pos.y, true);
		}
		if (!isLeftPressed && !isRightPressed && !isDownPressed && !isUpPressed) {
			playerBody.setLinearVelocity(0,0);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
			pickupOrThrow();
		}

		if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			Gdx.app.exit();
		}
	}

	// ACTIONS

	public void pickupOrThrow() {
		if (pickedBox == null) {
			if (pickupCooldown < 0) {
				pickupCooldown = PICKUP_START_COOLDOWN;
				Vector2 pos = getPlayerPos().cpy();
				pos.add(lastDirection.x * TILE_SIZE, lastDirection.y * TILE_SIZE);
				Rectangle pickupArea = new Rectangle(pos.x, pos.y, pickupAreaSize.x, pickupAreaSize.y);
				pickupPos = pos.cpy();

				for (Body box : boxes) {
					Vector2 boxPos = box.getPosition();
					Rectangle boxRect = new Rectangle(boxPos.x, boxPos.y, TILE_SIZE, TILE_SIZE);
					if (boxRect.overlaps(pickupArea)) {
						pickedBox = box;
					}
				}
			}
		} else {
			if (pickupCooldown < 0) {
				pickupCooldown = PICKUP_START_COOLDOWN;
				Vector2 pos = getPlayerPos().cpy().add(lastDirection.x * TILE_SIZE, lastDirection.y * TILE_SIZE);
				throwCurrentBox(pos, lastDirection.cpy().scl(BOX_FORCE, BOX_FORCE));
			}
		}
	}

	public void throwCurrentBox(Vector2 pos, Vector2 amount) {
		if (pickedBox != null) {
			pickedBox.setTransform(pos.x, pos.y, 0);
			pickedBoxPostion = null;
			thrownBox = pickedBox;
			thrownMove = amount;
			pickedBox = null;
			throwCooldown = THROW_COOLDOWN;
		}
	}

	private Vector2 getRandomEdge() {
		Vector2 pos = null;
		switch (MathUtils.random(3)) {
			case 0:
				pos = new Vector2(MathUtils.random(TARGET_WORLD_BUFFER, WORLD_WIDTH - TARGET_WORLD_BUFFER), TARGET_WORLD_BUFFER);
				break;
			case 1:
				pos = new Vector2(MathUtils.random(TARGET_WORLD_BUFFER, WORLD_WIDTH - TARGET_WORLD_BUFFER), WORLD_HEIGHT - TARGET_WORLD_BUFFER);
				break;
			case 2:
				pos = new Vector2(TARGET_WORLD_BUFFER, MathUtils.random(TARGET_WORLD_BUFFER, WORLD_HEIGHT - TARGET_WORLD_BUFFER));
				break;
			case 3:
				pos = new Vector2(WORLD_WIDTH - TARGET_WORLD_BUFFER, MathUtils.random(TARGET_WORLD_BUFFER, WORLD_HEIGHT - TARGET_WORLD_BUFFER));
				break;
		}
		return pos;
	}

	public void addTarget() {
		Vector2 pos = getRandomEdge();
		Rectangle rect = new Rectangle(pos.x, pos.y, TILE_SIZE, TILE_SIZE);
		targets.add(new Target(rect, "potato"));
	}

}
