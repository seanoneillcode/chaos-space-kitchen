package com.mygdx.game;

import static com.mygdx.game.TestGame.HALF_SIZE;
import static com.mygdx.game.TestGame.HEAVY_DENSITY;
import static com.mygdx.game.TestGame.LIGHT_DENSITY;
import static com.mygdx.game.TestGame.WORLD_HEIGHT;
import static com.mygdx.game.TestGame.WORLD_WIDTH;

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
        bodyDef.position.set(WORLD_WIDTH / 2.0f, WORLD_HEIGHT / 2.0f);

        Body body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        body.setBullet(true);
        body.setLinearDamping(1.0f);

        CircleShape shape = new CircleShape();
        shape.setRadius(4);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = LIGHT_DENSITY;
        fixtureDef.friction = 0.01f;

        Fixture fixture = body.createFixture(fixtureDef);

        playerBody = body;

        shape.dispose();

        return playerBody;
    }

    public MyBox createBox(World world, Vector2 position, Food type) {
        Body playerBody;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position.x, position.y);

        Body body = world.createBody(bodyDef);
        body.setFixedRotation(true);
        body.setBullet(true);
        body.setLinearDamping(3.0f);

        Vector2[] vertices = new Vector2[4];

        vertices[0] = new Vector2(-HALF_SIZE  , -HALF_SIZE  );
        vertices[1] = new Vector2(-HALF_SIZE , HALF_SIZE  );
        vertices[2] = new Vector2(HALF_SIZE , HALF_SIZE);
        vertices[3] = new Vector2(HALF_SIZE , -HALF_SIZE);

        PolygonShape shape = new PolygonShape();

        shape.set(vertices);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = HEAVY_DENSITY;
        fixtureDef.friction = 0.2f;

        Fixture fixture = body.createFixture(fixtureDef);

        playerBody = body;

        shape.dispose();

        return new MyBox(playerBody, type);
    }
}
