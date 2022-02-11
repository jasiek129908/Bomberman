package com.janek.Client.Sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BrickDestroyable extends Sprite {
    public float x;
    public float y;

    public BrickDestroyable(Sprite sprite, float x, float y) {
        super(sprite);
        this.x = x;
        this.y = y;
    }

    public void draw(SpriteBatch spriteBatch) {
        super.draw(spriteBatch);
    }
}
