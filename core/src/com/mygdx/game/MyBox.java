package com.mygdx.game;

import static com.mygdx.game.TestGame.fromBox2d;
import static com.mygdx.game.TestGame.toBox2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class MyBox {

    Body body;
    Food type;
    Vector2 drawPos;
    float lerpTimer;

    public MyBox(Body body, Food type) {
        this.body = body;
        this.type = type;
        this.drawPos = TestGame.fromBox2d(body.getPosition().cpy());
        this.lerpTimer = 0;
    }

    public void setPosition(Vector2 pos) {
        this.body.setTransform(toBox2d(pos), 0);
    }

    public Vector2 getPosition() {
        return fromBox2d(body.getPosition().cpy());
    }

    public void update() {
        if (lerpTimer > 0) {
            lerpTimer = lerpTimer - Gdx.graphics.getDeltaTime();
            Vector2 to = TestGame.fromBox2d(body.getPosition().cpy());
            drawPos.x = MathUtils.lerp(drawPos.x, to.x, 1.0f - (lerpTimer / TestGame.BOX_LERP_TIMER));
            drawPos.y = MathUtils.lerp(drawPos.y, to.y, 1.0f - (lerpTimer / TestGame.BOX_LERP_TIMER));
        } else {
            drawPos = TestGame.fromBox2d(body.getPosition());
        }
    }

}
