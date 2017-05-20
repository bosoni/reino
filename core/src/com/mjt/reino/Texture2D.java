package com.mjt.reino;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Texture2D
{
	private static AssetManager assetManager = null;
	private float xp = 0, yp = 0;
	public Sprite sprite;
	public float scale = 0.0f;

	public Texture2D()
	{
		if (assetManager == null)
			assetManager = new AssetManager();
	}

	/**
	 * lataa texture
	 * 
	 * @param fileName
	 * @param centerOrigin  jos true, origo on keskell�, muuten vasemmassa alanurkassa
	 */
	public void load(String fileName, boolean centerOrigin)
	{
		load(fileName, centerOrigin, false);
	}

	public void load(String fileName, boolean centerOrigin, boolean flipX)
	{
		assetManager.load("data/" + fileName, Texture.class);
		assetManager.finishLoading();
		Texture texture = assetManager.get("data/" + fileName);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		TextureRegion region = new TextureRegion(texture);
		if (flipX)
			region.flip(true, false);
		sprite = new Sprite(region);
		sprite.scale(scale);

		if (centerOrigin)
		{
			xp = sprite.getWidth() / 2;
			yp = sprite.getHeight() / 2;
		}
		sprite.setOrigin(xp, yp);
	}

	public void draw(float x, float y, SpriteBatch batch)
	{
		sprite.setPosition(-xp + x, -yp + y);
		sprite.draw(batch);
	}

}
