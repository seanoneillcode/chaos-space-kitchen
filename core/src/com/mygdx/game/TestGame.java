package com.mygdx.game;

import static com.badlogic.gdx.math.MathUtils.random;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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
	Texture img, boxImage, targetImage, potTargetImage, dogImage, potatoImage, runningDogImage, levelImage;

	public static final int WORLD_WIDTH = 360; // 480
	public static final int WORLD_HEIGHT = 240; // 360
	public static final int WORLD_BUFFER = 8;
	public static final int TARGET_WORLD_BUFFER = 24;
	private static final float PLAYER_SPEED = 5f;
	private static final float PICKUP_START_COOLDOWN = 0.2f;
	public static final float AIR_COOLDOWN = 0.5f;
	private static final float THROW_COOLDOWN = 0.5f;
	public static final float TILE_SIZE = 32.0f;
	public static final float HALF_SIZE = 16.0f;
	public static final float LIGHT_DENSITY = 0.01f;
	public static final float HEAVY_DENSITY = 0.04f;
	public static final float NEXT_LEVEL_TIMER = 4.0f;

	private static final float BOX_FORCE = 4.0f;
	public static final float THROW_FRICTION = 0.94f;

	public static final float FROM_BOX2D = 16f;
	public static final float TO_BOX2D = 0.0625f;
	public static final float PLAYER_SIZE = 8;
	public static final float ENEMY_SPEED = 3f;

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

	MyBox pickedBox;
	MyBox thrownBox;

	List<Target> targets;

	float pickupCooldown;
	float throwCooldown;

	int numBoxes = 0;
	int numEnemies = 0;
	int score = 0;
	int scoreTarget = 0;
	float levelCountdown = 0;
	float nextLevelCountdown = 0;
	int currentLevel = 0;
	LevelState levelState;

	private BitmapFont font;

	private Vector2 cameraPos;

	List<MyBox> boxes = new ArrayList<MyBox>();
	List<Enemy> enemies = new ArrayList<Enemy>();
	List<LevelData> levelDatas = new ArrayList<LevelData>();

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
		potTargetImage = new Texture("potTarget.png");
		potatoImage = new Texture("potato.png");
		dogImage = new Texture("dog.png");
		runningDogImage = new Texture("runningDog.png");
		levelImage = new Texture("level-01.png");

		// fonts
		loadFonts();

		// build levels
		levelDatas.add(new LevelData(0, 4, 3, 10));
		levelDatas.add(new LevelData(1, 4, 4, 11));
//		levelDatas.add(new LevelData(3, 3, 5, 12));
//		levelDatas.add(new LevelData(2, 6, 6, 13));
//		levelDatas.add(new LevelData(4, 6, 7, 14));
//		levelDatas.add(new LevelData(4, 7, 8, 15));
//		levelDatas.add(new LevelData(8, 0, 8, 20));
//		levelDatas.add(new LevelData(6, 8, 10, 25));
//		levelDatas.add(new LevelData(4, 12, 12, 30));

		// start game
		resetGame();
	}

	private void loadFonts() {
		FileHandle handle = Gdx.files.internal("roboto-regular.ttf");
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(handle);
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 20;
		font = generator.generateFont(parameter);
		font.setUseIntegerPositions(false);
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
		debugMatrix = new Matrix4(camera.combined.cpy().scl(FROM_BOX2D));
		debugRenderer = new Box2DDebugRenderer();
		batch.setProjectionMatrix(camera.combined);
	}

	@Override
	public void render () {
		camera.position.set(getLerpCamera());
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		handleInput();
		update();

		world.step(Gdx.graphics.getDeltaTime(), 6, 2);
		playerBody.setAwake(true);

		Gdx.gl.glClearColor(0f, 0f, 0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();


		Vector2 offset = new Vector2(camera.position.x, camera.position.y).sub(WORLD_WIDTH / 2.0f, WORLD_HEIGHT / 2.0f);
		batch.draw(levelImage, 0, 0);

		Vector2 pos = getDrawPlayerPos();

		for (Target target : targets) {
			if (target.type.equals(Food.DOG)) {
				batch.draw(potTargetImage, target.rect.x, target.rect.y);
			}
			if (target.type.equals(Food.POTATO)) {
				batch.draw(targetImage, target.rect.x, target.rect.y);
			}
		}

		for (MyBox box : boxes) {
			Vector2 boxPos = fromBox2d(box.body.getPosition());
			if (box.type.equals(Food.POTATO)) {
				batch.draw(potatoImage, boxPos.x, boxPos.y);
			}
			if (box.type.equals(Food.DOG)) {
				batch.draw(dogImage, boxPos.x, boxPos.y);
			}
		}

		for (Enemy enemy : enemies) {
			if (enemy.box.type.equals(Food.DOG)) {
				Vector2 enemyPos = enemy.getPos();
				batch.draw(runningDogImage, enemyPos.x, enemyPos.y);
			}
		}

		// draw player
		batch.draw(img, pos.x, pos.y);

		font.draw(batch, "" + score + "/" + scoreTarget, 20.0f + offset.x, 220.0f + offset.y);
		if (levelState == LevelState.PLAYING) {
			font.draw(batch, "" + (int) levelCountdown + "s", 300.0f + offset.x, 220.0f + + offset.y);
		}

		if (levelState == LevelState.OVER) {
			font.draw(batch, "" + (int) nextLevelCountdown + "s", 300.0f + offset.x, 220.0f + offset.y);
			font.draw(batch, "TOO MUCH PRESSURE", 80.0f + offset.x, 150.0f + offset.y);
			font.draw(batch, "press 'Enter' to play again", 70.0f + offset.x, 60.0f + offset.y);
		}
		if (levelState == LevelState.NEXT) {
			font.draw(batch, "" + (int) nextLevelCountdown + "s", 300.0f + offset.x, 220.0f + offset.y);
			font.draw(batch, "YOU DID IT!", 120.0f + offset.x, 150.0f + offset.y);
		}
		if (levelState == LevelState.WON) {
			font.draw(batch, "WELL DONE", 120.0f + offset.x, 190.0f + offset.y);
			font.draw(batch, "YOU SURVIVED THE CHAOS KITCHEN", 4.0f + offset.x, 150.0f + offset.y);
			font.draw(batch, "press 'Enter' to play again", 50.0f + offset.x, 60.0f + offset.y);
		}

		//batch.draw(boxImage, pickupPos.x, pickupPos.y);

		batch.end();

//		debugRenderer.render(world, new Matrix4(camera.combined.cpy().scl(FROM_BOX2D)));
	}

	private Vector3 getLerpCamera() {
		Vector2 pos = getPlayerPos();
		Vector3 target = new Vector3(pos.x, pos.y, 0);
		final float speed = 4.0f * Gdx.graphics.getDeltaTime();
		float ispeed = 1.0f - speed;
		Vector3 cameraPosition = camera.position.cpy();
		cameraPosition.scl(ispeed);
		target.scl(speed);
		cameraPosition.add(target);
		return cameraPosition;
	}


	public void resetGame() {
		currentLevel = 0;
		levelState = LevelState.PLAYING;
		loadNextLevel();
	}

	public void loadNextLevel() {
		for (MyBox box : boxes) {
			world.destroyBody(box.body);
		}
		boxes.clear();
		for (Enemy enemy : enemies) {
			world.destroyBody(enemy.box.body);
		}
		enemies.clear();
		if (playerBody != null) {
			world.destroyBody(playerBody);
		}
		if (currentLevel < levelDatas.size()) {
			LevelData levelData = levelDatas.get(currentLevel);
			numBoxes = levelData.numBoxes;
			numEnemies = levelData.numEnemies;
			scoreTarget = levelData.scoreTarget;
			score = 0;
			levelCountdown = levelData.levelCountdown;
			levelState = LevelState.PLAYING;
			resetLevel();
			currentLevel++;
		} else {
			// TODO YOU ARE WINRAR
			levelState = LevelState.WON;
		}
	}

	public void resetLevel() {
		lastDirection = new Vector2();
		inputVector = new Vector2();
		pickupPos = new Vector2();
		thrownMove = new Vector2();
		playerBody = entityFactory.createPlayer(world);
		boxes = new ArrayList<MyBox>();
		enemies = new ArrayList<Enemy>();
		targets = new ArrayList<Target>();
		pickupAreaSize = new Vector2(TILE_SIZE, TILE_SIZE);
		pickupCooldown = 0;
		throwCooldown = 0;
		pickedBox = null;
		thrownBox = null;
		List<Food> types = new ArrayList<Food>();
		types.add(Food.DOG);
		types.add(Food.POTATO);
		for (int i = 0; i < numBoxes; i++) {
			Vector2 randPos = new Vector2(
					random(WORLD_BUFFER,WORLD_WIDTH - WORLD_BUFFER),
					random(WORLD_BUFFER, WORLD_HEIGHT - WORLD_BUFFER));
			boxes.add(entityFactory.createBox(world, randPos, types.get(MathUtils.random(0, 1))));
		}
		for (int j = 0; j < numEnemies; j++) {
			Vector2 randPos = new Vector2(
					random(WORLD_BUFFER,WORLD_WIDTH - WORLD_BUFFER),
					random(WORLD_BUFFER, WORLD_HEIGHT - WORLD_BUFFER));

			enemies.add(entityFactory.createEnemy(world, randPos, Food.DOG));
		}
		addTarget(Food.DOG);
		addTarget(Food.POTATO);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}


	// UPDATE

	public void update() {
		if (levelState == LevelState.PLAYING && enemies.size() == 0 && boxes.size() == 0) {
			finishLevel();
		}

		if (levelCountdown > 0) {
			levelCountdown = levelCountdown - Gdx.graphics.getDeltaTime();
			if (levelCountdown < 0) {
				finishLevel();
			}
		}
		if (nextLevelCountdown > 0) {
			nextLevelCountdown = nextLevelCountdown - Gdx.graphics.getDeltaTime();
			if (nextLevelCountdown < 0) {
				loadNextLevel();
			}
		}
		setPlayerPos(worldConstrainedPosition(getPlayerPos()));
		for (MyBox box : boxes) {
			box.body.setTransform(
					toBox2d(worldConstrainedPosition(
							fromBox2d(box.body.getPosition().cpy()))), 0);
		}
		pickupCooldown = pickupCooldown - Gdx.graphics.getDeltaTime();
		throwCooldown = throwCooldown - Gdx.graphics.getDeltaTime();
		if (pickedBox != null) {
			Vector2 offset = getPlayerPos().cpy().add(0, TILE_SIZE + 4);
			pickedBoxPostion = offset;
			pickedBox.body.setTransform(toBox2d(offset), 0);
		}
		if (thrownBox != null) {
			if (throwCooldown < 0) {
				thrownBox = null;
			} else {
				thrownMove = thrownMove.scl(THROW_FRICTION);
				Vector2 pos = fromBox2d(thrownBox.body.getPosition().cpy()).add(thrownMove);
				thrownBox.body.setTransform(toBox2d(pos.x), toBox2d(pos.y), 0);
			}
		}
		for (Enemy enemy : enemies) {
			if (!(pickedBox != null && enemy.box == pickedBox)) {
				bounceOffEdge(enemy);
				enemy.update();
			} else {
				Vector2 offset = getPlayerPos().cpy().add(0, TILE_SIZE + 4);
				enemy.position = offset.cpy();
			}
		}

		for (Target target : targets) {

			Iterator<MyBox> iter = boxes.listIterator();
			while (iter.hasNext()) {
				MyBox thisBox = iter.next();
				Vector2 boxPos = fromBox2d(thisBox.body.getPosition());
				Rectangle boxRect = new Rectangle(boxPos.x, boxPos.y, TILE_SIZE, TILE_SIZE);


				if (boxRect.overlaps(target.rect) && target.type.equals(thisBox.type)) {
					iter.remove();
					world.destroyBody(thisBox.body);
					score = score + 1;
				}
			}

			Iterator<Enemy> iter2 = enemies.listIterator();
			while (iter2.hasNext()) {
				Enemy enemy = iter2.next();
				if (enemy.airCooldown > 0) {
					Vector2 boxPos = fromBox2d(enemy.box.body.getPosition());
					Rectangle boxRect = new Rectangle(boxPos.x, boxPos.y, TILE_SIZE, TILE_SIZE);
					if (boxRect.overlaps(target.rect) && target.type.equals(enemy.box.type)) {
						world.destroyBody(enemy.box.body);
						iter2.remove();
						score = score + 1;
					}
				}
			}
		}

	}

	public void finishLevel() {
		if (score < scoreTarget) {
			levelState = LevelState.OVER;
		} else {
			nextLevelCountdown = NEXT_LEVEL_TIMER;
			levelState = LevelState.NEXT;
		}
	}

	public void bounceOffEdge(Enemy enemy) {
		Vector2 pos = enemy.getPos();
		Vector2 mov = enemy.movement;
		if (pos.x < WORLD_BUFFER) {
			mov.x = mov.x * -1;
		}
		if (pos.x > WORLD_WIDTH - WORLD_BUFFER) {
			mov.x = mov.x * -1;
		}
		if (pos.y < WORLD_BUFFER) {
			mov.y = mov.y * -1;
		}
		if (pos.y > WORLD_HEIGHT - WORLD_BUFFER) {
			mov.y = mov.y * -1;
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
		playerBody.setTransform(toBox2d(pos.cpy()), 0);
	}

	private void setPlayerPos(float x, float y) {
		setPlayerPos(new Vector2(x, y));
	}

	private Vector2 getPlayerPos() {
		return fromBox2d(playerBody.getPosition().cpy());
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

		Vector2 pos = fromBox2d(playerBody.getPosition());
		inputVector.x = 0;
		inputVector.y = 0;

		if (isLeftPressed) {
			inputVector.x = inputVector.x - 1;
			lastDirection = inputVector.cpy();
			playerBody.applyLinearImpulse(toBox2d(-actualSpeed), 0, toBox2d(pos.x), toBox2d(pos.y), true);
		}
		if (isRightPressed) {
			inputVector.x = inputVector.x + 1;
			lastDirection = inputVector.cpy();
			playerBody.applyLinearImpulse(toBox2d(actualSpeed), 0, toBox2d(pos.x), toBox2d(pos.y), true);
		}
		if (isUpPressed) {
			inputVector.y = inputVector.y + 1;
			lastDirection = inputVector.cpy();
			playerBody.applyLinearImpulse(0, toBox2d(actualSpeed), toBox2d(pos.x), toBox2d(pos.y), true);
		}
		if (isDownPressed) {
			inputVector.y = inputVector.y - 1;
			lastDirection = inputVector.cpy();
			playerBody.applyLinearImpulse(0, toBox2d(-actualSpeed), toBox2d(pos.x), toBox2d(pos.y), true);
		}
//		if (!isLeftPressed && !isRightPressed && !isDownPressed && !isUpPressed) {
//			playerBody.setLinearVelocity(0,0);
//		}

		if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
			pickupOrThrow();
		}
		if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			Gdx.app.exit();
		}
		if (Gdx.input.isKeyPressed(Input.Keys.ENTER) && (levelState == LevelState.WON || levelState == LevelState.OVER) ) {
			resetGame();
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
				checkPick(pickupArea);
				if (pickedBox == null) {
					pos = getPlayerPos().cpy().sub(HALF_SIZE, HALF_SIZE);
					pickupArea = new Rectangle(pos.x, pos.y, 2 * TILE_SIZE, 2 * TILE_SIZE);
					checkPick(pickupArea);
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

	public void checkPick(Rectangle pickupArea) {
		for (MyBox box : boxes) {
			Vector2 boxPos = fromBox2d(box.body.getPosition());
			Rectangle boxRect = new Rectangle(boxPos.x, boxPos.y, TILE_SIZE, TILE_SIZE);
			if (boxRect.overlaps(pickupArea)) {
				pickedBox = box;
			}
		}
		for (Enemy enemy : enemies) {
			MyBox box = enemy.box;
			Vector2 boxPos = fromBox2d(box.body.getPosition());
			Rectangle boxRect = new Rectangle(boxPos.x, boxPos.y, TILE_SIZE, TILE_SIZE);
			if (boxRect.overlaps(pickupArea)) {
				pickedBox = box;
			}
		}
	}

	public void throwCurrentBox(Vector2 pos, Vector2 amount) {
		if (pickedBox != null) {
			for (Enemy enemy : enemies) {
				if (enemy.box.equals(pickedBox)) {
					enemy.movement = amount.cpy();
					enemy.position = pos.cpy();
					enemy.airCooldown = AIR_COOLDOWN;
				}
			}
			pickedBox.body.setTransform(toBox2d(pos.x), toBox2d(pos.y), 0);
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

	public void addTarget(Food type) {
		Vector2 pos = getRandomEdge();
		Rectangle rect = new Rectangle(pos.x, pos.y, TILE_SIZE, TILE_SIZE);
		targets.add(new Target(rect, type));
	}

	public static float toBox2d(float realWorld) {
		return realWorld * TO_BOX2D;
	}

	public static Vector2 toBox2d(Vector2 realWorld) {
		return realWorld.scl(TO_BOX2D);
	}

	public static float fromBox2d(float box2dWorld) {
		return box2dWorld * FROM_BOX2D;
	}

	public static Vector2 fromBox2d(Vector2 box2dWorld) {
		return box2dWorld.scl(FROM_BOX2D);
	}

}
