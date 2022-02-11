package com.janek.Client.Sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.janek.Client.Bomberman;

public class Bomber extends Sprite {
    public String id;
    public String nickName;
    public float x;
    public float y;
    public Bomberman.Direction direction;
    public boolean ready;
    public boolean isMoving;
    public int bombAvailable;
    public boolean isDeath;
    public boolean isWinner;
    public float animationTimer;
    public Bomberman.Direction lastDirection;
    public int numberOfLastAnimationFrame;


    public Bomber(Sprite sprite) {
        super(sprite);
        this.direction = Bomberman.Direction.DOWN;
        this.ready = false;
        this.numberOfLastAnimationFrame = 0;
        this.bombAvailable = 2;
        this.isDeath = false;
        this.isWinner = false;
        this.id = "-1";
    }

    public void draw(SpriteBatch spriteBatch) {
        super.draw(spriteBatch);
    }

}
