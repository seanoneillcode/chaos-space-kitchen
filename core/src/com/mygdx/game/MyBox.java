package com.mygdx.game;

import com.badlogic.gdx.physics.box2d.Body;

public class MyBox {

    Body body;
    Food type;

    public MyBox(Body body, Food type) {
        this.body = body;
        this.type = type;
    }

}
