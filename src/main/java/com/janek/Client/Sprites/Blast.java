package com.janek.Client.Sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Blast extends Sprite {
    public static final float BLAST_DURATION = 2f;
    public float x;
    public float y;
    public int bombId;
    public float timeElapsed;

    public Blast(Sprite sprite, float x, float y, int bombId) {
        super(sprite);
        this.x = x;
        this.y = y;
        this.bombId = bombId;
        this.timeElapsed = 0;
    }

    public void draw(SpriteBatch spriteBatch) {
        super.draw(spriteBatch);
    }
}
