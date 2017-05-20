// Reino (c) 2013-2014  mjt & oajk 
// code:  mjt
// image: soajk
package com.mjt.reino;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Reino implements ApplicationListener
{
	private OrthographicCamera camera;
	private SpriteBatch batch;
	BitmapFont font;
	GlyphLayout glyphLayout = new GlyphLayout();
	Player player = new Player();
	Texture2D mousePointer = new Texture2D();
	Texture2D itemPointer = null;
	TextboxNinePatch textBox;
	boolean starting=true;
	Texture2D alku=new Texture2D();
	boolean gameOver = false;
		
	@Override
	public void create()
	{
		textBox = TextboxNinePatch.getInstance();

		font = new BitmapFont();
		font.setColor(1, 1, 1, 1);

		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		batch = new SpriteBatch();

		mousePointer.load("pointer.png", false);
		Gdx.input.setCursorCatched(true);

		Room.curRoom = new Room();
		Room.curRoom.loadRoom("bg0");
		player.load("u2_", 1);
		player.x = Room.curRoom.SX;
		player.y = Room.curRoom.SY;
		
		alku.load("alku.jpg", false); // lataa alkukuva
	}

	@Override
	public void dispose()
	{
		batch.dispose();
	}

	@Override
	public void render()
	{
		if (Gdx.input.isKeyPressed(Keys.ESCAPE))
			Gdx.app.exit();

		if(starting)
		{
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			alku.draw(0, 0, batch);			
			batch.end();
			if (Gdx.input.isButtonPressed(Buttons.LEFT))
			{
				starting=false;
			}
			else
				return;
		}
		
		Util.updateMouse();

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		Room.curRoom.draw(batch);

		if (gameOver == false)
			player.draw(batch);

		if (Player.text.length() == 0 && gameOver == false)
			updateGame();
		else
		{

			// HACK  -- näin saadaan peli läpi
			if (Player.text.startsWith("avasit") && gameOver == false)
			{
				gameOver = true;
				Player.text = Player.text + "\nmenit ovesta kohti uusia seikkailuja.\npeli lï¿½pi!";
			}
			//-----------------------------------------

			if (Player.text.length() > 0)
			{
				glyphLayout.setText(font, Player.text);
				
				float w = glyphLayout.width + 60; 
				float h = glyphLayout.height + 20; 
				textBox.draw(this.batch, Gdx.graphics.getWidth() / 2 - w / 2, Gdx.graphics.getHeight() - 50 - h + 10, w, h);
				font.draw(batch, Player.text, Gdx.graphics.getWidth() / 2 - w / 2 + 30, Gdx.graphics.getHeight() - 50);
			}

			if (Gdx.input.justTouched())
				Player.text = "";
		}

		//System.out.println(">> "+Gdx.input.getY());
		
		int x = Gdx.input.getX(), y = Gdx.graphics.getHeight() - Gdx.input.getY();

		String txt = Room.curRoom.pointInPolygon(x, y);
		if (txt.equals("OK") == false && txt.startsWith("GOTO") == false)
			font.draw(batch, txt.split("_")[0], x - 10, y + 15);

		if (itemPointer != null && Player.usingItem.length() > 0)
			itemPointer.draw(x + 20, y - (int) itemPointer.sprite.getHeight() - 15, batch);
		
		mousePointer.draw(x, y - (int) mousePointer.sprite.getHeight(), batch);

		batch.end();

	}

	void updateGame()
	{
		int x = Gdx.input.getX(), y = Gdx.graphics.getHeight() - Gdx.input.getY();

		if (Gdx.input.isButtonPressed(Buttons.LEFT)) // walk, get, use
		{
			if (y < 70 && x < Player.inventory.size() * Player.INVENTORY_OBJ_WIDTH)
			{
				String objName = Player.inventory.get(x / Player.INVENTORY_OBJ_WIDTH);
				itemPointer = AllItems.getTexture(objName);
				Player.usingItem = objName;
				return;
			}

			Player.text = "";
			player.walkTo(x, y);
		}
		else if (Gdx.input.isButtonPressed(Buttons.RIGHT)) // look
		{
			if (itemPointer != null)
			{
				itemPointer = null;
				Player.usingItem = "";
				return;
			}

			Player.text = "";
			player.lookAt(x, y);
		}

	}

	@Override
	public void resize(int width, int height)
	{
		Gdx.input.setCursorPosition(width / 2, height / 2);
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
	}
}

class TextboxNinePatch extends NinePatch
{
	private static TextboxNinePatch instance;

	private TextboxNinePatch()
	{
		super(new Texture(Gdx.files.internal("data/menuskin.png")), 8, 8, 8, 8);
	}

	public static TextboxNinePatch getInstance()
	{
		if (instance == null)
		{
			instance = new TextboxNinePatch();
		}
		return instance;
	}
}
