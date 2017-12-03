package com.mygdx.game;

import java.util.List;

public class LevelData {
    public int numEnemies;
    public int numBoxes;
    public int scoreTarget;
    public float levelCountdown;
    public List<Food> levelFood;

    public LevelData(int numEnemies, int numBoxes, int scoreTarget, float levelCountdown, List<Food> levelFood) {
        this.numEnemies = numEnemies;
        this.numBoxes = numBoxes;
        this.scoreTarget = scoreTarget;
        this.levelCountdown = levelCountdown;
        this.levelFood = levelFood;
    }
}


