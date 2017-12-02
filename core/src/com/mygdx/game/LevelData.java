package com.mygdx.game;

public class LevelData {
    public int numEnemies;
    public int numBoxes;
    public int scoreTarget;
    public float levelCountdown;

    public LevelData(int numEnemies, int numBoxes, int scoreTarget, float levelCountdown) {
        this.numEnemies = numEnemies;
        this.numBoxes = numBoxes;
        this.scoreTarget = scoreTarget;
        this.levelCountdown = levelCountdown;
    }
}


