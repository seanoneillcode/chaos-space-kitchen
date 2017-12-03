package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;

public class Target {

    public Rectangle rect;
    public Food type;
    public float effectTimer;

    public Target(Rectangle rect, Food type) {
        this.rect = rect;
        this.type = type;
        this.effectTimer = 0;
    }

    public void causeSmokeEffect() {
        effectTimer = 0.8f;
    }

    public void update() {
        if (effectTimer > 0) {
            effectTimer = effectTimer - Gdx.graphics.getDeltaTime();
        }
    }
}
