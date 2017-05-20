package com.mjt.reino;

import java.util.ArrayList;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Player
{
	static final int INVENTORY_OBJ_WIDTH = 80;

	private ArrayList<Texture2D> up = new ArrayList<Texture2D>();
	private ArrayList<Texture2D> down = new ArrayList<Texture2D>();
	private ArrayList<Texture2D> left = new ArrayList<Texture2D>();
	private ArrayList<Texture2D> right = new ArrayList<Texture2D>();
	int curImg = 0, time = 0;
	float x = 0, y = 0;
	float toX = 0, toY = 0;
	boolean action = false;
	int direction = 1;
	static String text = "";
	static ArrayList<String> inventory = new ArrayList<String>();
	static ArrayList<String> hideObj = new ArrayList<String>();
	static String usingItem = "";

	public void load(String prefix, int count)
	{
		final float scale = -0.3f;
		for (int q = 1; q < count + 1; q++)
		{
			Texture2D tex = new Texture2D();
			tex.scale = scale;
			tex.load(prefix + "u" + q + ".png", false);
			up.add(tex);

			tex = new Texture2D();
			tex.scale = scale;
			tex.load(prefix + "d" + q + ".png", false);
			down.add(tex);

			tex = new Texture2D();
			tex.scale = scale;
			tex.load(prefix + "l" + q + ".png", false);
			left.add(tex);

			tex = new Texture2D();
			tex.scale = scale;
			tex.load(prefix + "l" + q + ".png", false, true); // flipataan X
			right.add(tex);
		}
	}

	/**
	 * piirt�� ukon, animoi ja muuttaa positionia jos se on liikkeell�.
	 * piirt�� my�s inventoryn
	 * @param batch 
	 */
	public void draw(SpriteBatch batch)
	{
		// direction 1=up, 2=down, 3=left, 4=right
		float xfix = 10;
		switch (direction)
		{
		case 1:
			up.get(curImg).draw(x - xfix, y, batch);
			break;
		case 2:
			down.get(curImg).draw(x - xfix, y, batch);
			break;
		case 3:
			left.get(curImg).draw(x - xfix, y, batch);
			break;
		case 4:
			right.get(curImg).draw(x - xfix, y, batch);
			break;

		}

		if (action)
		{
			// tarkista jos painettu jotain polya, niin lasketaan lyhin et�isyys
			// pelaajan ja polyn v�lille. jos tarpeeks l�hell�, ota/k�yt�/puhu toiminto
			float len = Room.curRoom.calcLen(x + 20, y + 60, toX, toY);
			float len2 = Room.curRoom.calcLen(x + 20, y, toX, toY);
			if (len < 50 || len2 < 50)
			{
				String str = Room.curRoom.pointInPolygon((int) toX, (int) toY);
				String dta[] = str.split("_");
				if (dta.length > 0 && dta[0].equals("") == false && dta[0].startsWith("OK") == false && dta[0].startsWith("GOTO") == false)
				{
					text = Room.curRoom.action(dta);
					action = false;
				}
			}

			// laske et�isyys pelaajan ja klikatun paikan v�lille
			Vector2 pos = new Vector2(x, y);
			Vector2 to = new Vector2(toX, toY);
			len = pos.dst(to);

			if (len > 10)
			{
				if (toX > x)
					direction = 4;
				else
					direction = 3;

				final float R = 30;
				float xx = x - toX;
				if (xx > -R && xx < R)
				{
					if (y < toY)
						direction = 1;
					if (y > toY)
						direction = 2;
				}

				final float SPEED = 2;
				Vector2 dir = to.sub(pos).nor();
				pos = pos.add(dir.scl(SPEED));

				// voiko liikkua?
				String str = Room.curRoom.pointInPolygon((int) pos.x, (int) pos.y);
				if (str.equals("OK"))
				{
					x = pos.x;
					y = pos.y;
					update(10);
				}

				if (str.startsWith("GOTO"))
				{
					String dta[] = str.split("_");
					Room.curRoom = new Room();
					Room.curRoom.loadRoom(dta[1]);
					action = false;
					text = "";
					x = Room.curRoom.SX;
					y = Room.curRoom.SY;
				}
			}
		}
		drawInventory(batch);
	}

	void drawInventory(SpriteBatch batch)
	{
		for (int q = 0; q < inventory.size(); q++)
		{
			final int x = q * INVENTORY_OBJ_WIDTH, y = 0;

			String objName = inventory.get(q);
			Texture2D tex = AllItems.getTexture(objName);

			if (tex != null)
				tex.draw(x, y, batch);
		}
	}

	public void update(int time)
	{
		this.time++;
		if (this.time >= time)
		{
			this.time = 0;
			curImg++;
			if (curImg >= up.size())
				curImg = 0;
		}
	}

	public void walkTo(int px, int py)
	{
		toX = px;
		toY = py;
		action = true;
	}

	public void lookAt(int x, int y)
	{
		String str = Room.curRoom.pointInPolygon(x, y);
		String dta[] = str.split("_");
		if (dta.length > 0 && dta[0].equals("") == false)
		{
			if (dta[0].startsWith("GOTO") || dta[0].startsWith("OK"))
				return;

			text = Room.curRoom.look(dta);
		}
	}

}
