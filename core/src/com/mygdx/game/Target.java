package com.mygdx.game;


import com.badlogic.gdx.math.Rectangle;

public class Target {

    public Rectangle rect;
    public String type;

    public Target(Rectangle rect, String type) {
        this.rect = rect;
        this.type = type;
    }
}
