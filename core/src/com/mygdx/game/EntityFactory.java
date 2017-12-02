package com.mygdx.game;

import static com.mygdx.game.TestGame.ENEMY_SPEED;
import static com.mygdx.game.TestGame.HALF_SIZE;
import static com.mygdx.game.TestGame.HEAVY_DENSITY;
import static com.mygdx.game.TestGame.LIGHT_DENSITY;
import static com.mygdx.game.TestGame.PLAYER_SIZE;
import static com.mygdx.game.TestGame.WORLD_HEIGHT;
import static com.mygdx.game.TestGame.WORLD_WIDTH;
import static com.mygdx.game.TestGame.toBox2d;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class EntityFactory {

    public Body createPlayer(World world) {
        Body playerBody;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(toBox2d(WORLD_WIDTH / 2.0f), toBox2d(WORLD_HEIGHT / 2.0f));

        Body body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        body.setBullet(true);
        body.setLinearDamping(3.0f);

        CircleShape shape = new CircleShape();
        shape.setRadius(toBox2d(PLAYER_SIZE));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = LIGHT_DENSITY;
        fixtureDef.friction = 1f;

        Fixture fixture = body.createFixture(fixtureDef);

        playerBody = body;
        playerBody.setUserData(MathUtils.random());

        shape.dispose();

        return playerBody;
    }

    public MyBox createBox(World world, Vector2 position, Food type) {
        Body playerBody;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(toBox2d(position.x), toBox2d(position.y));

        Body body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        body.setBullet(true);
        body.setLinearDamping(3.0f);

        Vector2[] vertices = new Vector2[4];

        vertices[0] = new Vector2(toBox2d(-HALF_SIZE)  , toBox2d(-HALF_SIZE)  );
        vertices[1] = new Vector2(toBox2d(-HALF_SIZE) , toBox2d(HALF_SIZE)  );
        vertices[2] = new Vector2(toBox2d(HALF_SIZE) , toBox2d(HALF_SIZE));
        vertices[3] = new Vector2(toBox2d(HALF_SIZE) , toBox2d(-HALF_SIZE));

        PolygonShape shape = new PolygonShape();

        shape.set(vertices);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = HEAVY_DENSITY;
        fixtureDef.friction = 1f;

        Fixture fixture = body.createFixture(fixtureDef);

        playerBody = body;

        shape.dispose();

        return new MyBox(playerBody, type);
    }

    public Enemy createEnemy(World world, Vector2 position, Food type) {
        MyBox box = createBox(world, position, type);
        Enemy enemy = new Enemy(box, new Vector2(MathUtils.random(-ENEMY_SPEED, ENEMY_SPEED), MathUtils.random(-ENEMY_SPEED, ENEMY_SPEED)).clamp(ENEMY_SPEED, ENEMY_SPEED), position.cpy());
        return enemy;
    }
}
