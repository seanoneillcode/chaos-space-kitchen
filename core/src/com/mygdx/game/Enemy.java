package com.mygdx.game;

import static com.mygdx.game.TestGame.ENEMY_SPEED;
import static com.mygdx.game.TestGame.THROW_FRICTION;
import static com.mygdx.game.TestGame.toBox2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Enemy {

    MyBox box;
    Vector2 movement;
    Vector2 position;
    float airCooldown;

    public Enemy(MyBox box, Vector2 movement, Vector2 position) {
        this.box = box;
        this.movement = movement;
        this.position = position;
    }

    public void update() {
        if (airCooldown < 0) {

        } else {
            airCooldown = airCooldown - Gdx.graphics.getDeltaTime();
            movement = movement.scl(THROW_FRICTION);
            if (airCooldown < 0) {
                movement = new Vector2(MathUtils.random(-ENEMY_SPEED, ENEMY_SPEED),
                        MathUtils.random(-ENEMY_SPEED, ENEMY_SPEED)).clamp(ENEMY_SPEED, ENEMY_SPEED);
            }
        }
        this.position = this.position.cpy().add(this.movement);
        box.body.setTransform(toBox2d(position.cpy()), 0);
    }

    public Vector2 getPos() {
        return this.position;
    }
}
