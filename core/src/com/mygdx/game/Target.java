package com.mygdx.game;


import com.badlogic.gdx.math.Rectangle;

public class Target {

    public Rectangle rect;
    public Food type;

    public Target(Rectangle rect, Food type) {
        this.rect = rect;
        this.type = type;
    }
}
