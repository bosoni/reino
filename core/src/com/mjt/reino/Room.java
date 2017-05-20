package com.mjt.reino;

import java.util.ArrayList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

//import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class Room
{
	static Room curRoom = null;
	static Item selectedObj = null;
	ArrayList<Item> objs = new ArrayList<Item>();
	public float SX, SY;

	public void loadRoom(String name)
	{
		curRoom = this;

		FileHandle handle = Gdx.files.internal("data/" + name + ".obj");
		String[] lines = handle.readString().split("\n");

		int line = 0;
		while (true)
		{
			// new mesh
			if (lines[line].startsWith("o"))
			{
				Item mesh = new Item();
				mesh.name = lines[line].split(" ")[1];

				// ota kaikki vertexit
				while (true)
				{
					if (lines[line + 1].startsWith("v ") == false)
						break;

					String[] vert = lines[line + 1].split(" ");
					// [-1,1] -> [screenwidth, screenheight]
					float x = ((Float.parseFloat(vert[1]) + 1) * 0.5f) * Gdx.graphics.getWidth();
					float y = Float.parseFloat(vert[2]);
					float z = Gdx.graphics.getHeight() - (((Float.parseFloat(vert[3]) + 1) * 0.5f) * Gdx.graphics.getHeight());
					mesh.points.add(new Vector3(x, y, z));
					line++;
				}
				if (mesh.points.size() == 4) // hack. jos rect, muuta parin vertexin paikkaa
				{
					mesh.points.add(2, mesh.points.get(3));
					mesh.points.remove(4);
				}

				// eti materiaali / seuraavan obun alku
				while (lines[line + 1].startsWith("usemtl ") == false && lines[line + 1].startsWith("o ") == false)
					line++;

				// jos seuraava mesh
				if (lines[line + 1].startsWith("o "))
				{
					objs.add(mesh);
					continue;
				}

				// jos materiaalitiedot niin ota ne 
				String[] mat = lines[line + 1].split(" ");
				if (mat.length > 1 && mat[1].equals("_NONE") == false)
					if (mat[1].startsWith("_"))
						if (mat[1].contains(".png"))
							mesh.useMat = mat[1].substring(1, mat[1].indexOf(".png") + 4);
						else
							mesh.useMat = mat[1].substring(1, mat[1].indexOf(".jpg") + 4);

				// luo texture jos meshill� on materiaali
				if (mesh.useMat != "")
				{
					mesh.tex = new Texture2D();
					mesh.tex.load(mesh.useMat, false);

					// lev ja kor
					float w = mesh.points.get(0).x - mesh.points.get(1).x;
					float h = mesh.points.get(2).z - mesh.points.get(0).z;

					mesh.tex.sprite.setBounds(0, 0, w, h);
				}
				objs.add(mesh);

				if (mesh.name.contains("START"))
				{
					SX = mesh.points.get(0).x;
					SY = mesh.points.get(0).z;
				}
			}

			line++;
			if (line == lines.length)
				break;
		}

		// poista ruudulta obut jotka on jo otettu / inventoryss�
		int q = 0;
		while (true)
		{
			String qname[] = objs.get(q).name.split("_");
			for (int w = 0; w < Player.inventory.size(); w++)
			{
				if (qname[0].equals(Player.inventory.get(w)))
				{
					objs.remove(q);
					q = -1;
					break;
				}
			}

			for (int w = 0; w < Player.hideObj.size(); w++)
			{
				if (qname[0].equals(Player.hideObj.get(w)))
				{
					objs.remove(q);
					q = -1;
					break;
				}
			}
			q++;
			if (q == objs.size())
				break;
		}

		loadInfos(name);
	}

	public void draw(SpriteBatch batch)
	{
		for (Item o : objs)
			if (o.name.contains("BG"))
			{
				o.tex.draw((int) o.points.get(1).x, (int) o.points.get(1).z, batch);
			}
		// ## HACK  eka etit�� BG ja piirret��n se, sen j�lkee muut. 
		// BG on viimesen� obj filussa joten se piirtyis muuten kaikkein muiden p��lle.
		// FIX: layerit (Z arvot) ja quicksortataan ne ja rendataan niiden perusteella. mut se sit joskus

		for (Item o : objs)
		{
			if (o.tex != null)
			{
				if (o.name.contains("BG") == false)
				{
					o.tex.draw((int) o.points.get(1).x, (int) o.points.get(1).z, batch);
				}

			}
		}
	}

	public String look(String[] datas)
	{
		for (Item o : objs)
		{
			if (o.name.split("_")[0].equals(datas[0]))
			{
				String desc = o.descTxt.get(o.descIdx);
				o.descIdx++;
				if (o.descIdx >= o.descTxt.size())
					o.descIdx = 0;
				return desc;
			}
		}
		return "no seh�n on " + datas[0];
	}

	public String action(String[] datas)
	{
		for (int q = 0; q < objs.size(); q++)
		{
			Item o = objs.get(q);

			if (o.name.split("_")[0].equals(datas[0]))
			{
				// jos ollaan k�ytt�m�ss� jotain tavaraa
				if (Player.usingItem.length() != 0)
				{
					for (int w = 0; w < o.useItem.size(); w++)
					{
						if (o.useItem.get(w).name.equals(Player.usingItem))
						{
							UseItem item = o.useItem.get(w);

							// poista obu inventorysta
							if (item.remove.length() > 0)
							{
								Player.inventory.remove(item.name);
								Player.hideObj.add(item.name);
								Player.usingItem = "";
							}

							// lis�� obu inventoryyn
							if (item.newItem.length() > 0)
							{
								Player.inventory.add(item.newItem);
							}

							return item.text;
						}
					}
					return "ei k�y!";
				}

				// otetaanko tavara
				if (o.takeTxt.length() > 0)
				{
					// h�vit� obu ruudulta
					String name[] = o.name.split("_");
					if (name[0].equals(datas[0]))
					{
						objs.remove(q);
					}
					Player.inventory.add(datas[0]);

					return o.takeTxt + "\n" + datas[0] + " otettu.";
				}

				if (o.touchTxt.size() == 0)
					return "";

				String touch = o.touchTxt.get(o.touchIdx);
				o.touchIdx++;
				if (o.touchIdx >= o.touchTxt.size())
					o.touchIdx = 0;
				return touch;
			}
		}
		return "eip.";
	}

	public float calcLen(float x, float y, float toX, float toY)
	{
		// saadaan selectedObj
		pointInPolygon((int) toX, (int) toY);
		float len = 9999999;
		if (selectedObj != null)
		{
			Vector2 pos = new Vector2(x, y);
			for (int q = 0; q < selectedObj.points.size(); q++)
			{
				Vector2 to = new Vector2(selectedObj.points.get(q).x, selectedObj.points.get(q).z);
				float len2 = pos.dst(to);
				if (len2 < len)
					len = len2;
			}
		}
		return len;
	}

	/**
	 * tarkista onko xy kohta polygonin sis�ll�.
	 * http://local.wasp.uwa.edu.au/~pbourke/geometry/insidepoly/
	 */
	public String pointInPolygon(int x, int y)
	{
		String polyInfo = "";
		selectedObj = null;

		for (Item o : objs)
		{
			int i, j;
			boolean c = false;
			for (i = 0, j = o.points.size() - 1; i < o.points.size(); j = i++)
			{
				Vector3 v1 = o.points.get(i);
				Vector3 v2 = o.points.get(j);
				if ((((v1.z <= y) && (y < v2.z)) || ((v2.z <= y) && (y < v1.z))) && (x < (v2.x - v1.x) * (y - v1.z) / (v2.z - v1.z) + v1.x))
					c = !c;
			}

			if (c)
			{
				if (o.name.contains("BG") || o.name.contains("START"))
					continue;

				if (o.name.contains("OK"))
				{
					if (polyInfo.length() == 0)
						polyInfo = "OK";
				}
				else
				{
					// jos hiiren alla on useampia objekteja, tsekataan syvyysarvo
					// ja valitaan l�himp�n� kameraa oleva (Y==depth, koska k�ytet��n XZ tasoa)
					// (mit� suurempi luku, sit� l�hemp�n� kameraa)
					if (selectedObj == null || o.points.get(0).y > selectedObj.points.get(0).y)
					{
						polyInfo = o.name;
						selectedObj = o;
					}
				}
				/*
				//--------------------------------------------
				if (false) // -- DEBUG ------------------------
				{
					shapeRenderer.begin(ShapeType.Line);
					shapeRenderer.setColor(1, 1, 0, 1);
					for (int q = 0; q < o.points.size() - 1; q++)
					{
						int x1 = (int) o.points.get(q).x;
						int y1 = (int) o.points.get(q).z;
						int x2 = (int) o.points.get(q + 1).x;
						int y2 = (int) o.points.get(q + 1).z;
						shapeRenderer.line(x1, y1, x2, y2);
					}
					int x1 = (int) o.points.get(o.points.size() - 1).x;
					int y1 = (int) o.points.get(o.points.size() - 1).z;
					int x2 = (int) o.points.get(0).x;
					int y2 = (int) o.points.get(0).z;
					shapeRenderer.line(x1, y1, x2, y2);
					shapeRenderer.end();
				}*/
			}
		}
		return polyInfo;
	}

	static final ShapeRenderer shapeRenderer = new ShapeRenderer(); // ----- DEBUG ---------

	public void loadInfos(String name)
	{
		FileHandle handle = Gdx.files.internal("data/" + name + ".txt");
		String[] lines = handle.readString().split("\n");
		int line = 0;
		Item curObj = null;
		UseItem use = null;

		while (line < lines.length)
		{
			String[] data = lines[line++].split(":");
			for (int q = 0; q < data.length; q++)
				data[q] = data[q].trim();

			if (data[0].startsWith("#") || data.length < 2)
				continue;

			if (data[0].startsWith("NAME"))
			{
				for (Item o : objs)
				{
					if (o.name.split("_")[0].equals(data[1]))
					{
						curObj = o;
						use = null;
						break;
					}
				}
			}

			if (data[0].startsWith("LOOK"))
				curObj.descTxt.add(data[1]);
			if (data[0].startsWith("TOUCH"))
				curObj.touchTxt.add(data[1]);
			if (data[0].startsWith("TAKE"))
			{
				curObj.takeTxt = data[1];
				AllItems.add(curObj.name.split("_")[0], curObj.tex);
			}

			if (data[0].startsWith("USE"))
			{
				use = new UseItem();
				use.name = data[1].trim(); // objektin nimi jota k�ytet��n
				curObj.useItem.add(use);
			}
			// seuraavilta riveilt� l�ytyy TEXT:  REMOVE:  ADD:  tarpeen mukaan
			if (data[0].trim().startsWith("TEXT"))
				use.text = data[1];
			if (data[0].trim().startsWith("REMOVE"))
				use.remove = data[1];
			if (data[0].trim().startsWith("ADD"))
			{
				String l[] = data[1].trim().split(" ");
				use.newItem = l[0];
				if (l.length > 1)
				{
					Texture2D tex = new Texture2D();
					tex.load(l[1], false);
					AllItems.add(use.newItem, tex);
				}
			}
		}
	}
}

class Item
{
	public String name;
	public Texture2D tex;
	public ArrayList<Vector3> points = new ArrayList<Vector3>(); // poly
	public String useMat = "";

	ArrayList<String> descTxt = new ArrayList<String>(); // kun katsoo, n�ytet��n t�m�
	ArrayList<String> touchTxt = new ArrayList<String>(); // kun koskee, n�ytet��n t�m�
	String takeTxt = ""; // jos objekti on otettava, niin t�ss� ottamisen j�lkeinen teksti
	int descIdx = 0, touchIdx = 0;

	ArrayList<UseItem> useItem = new ArrayList<UseItem>();
}

class AllItems
{
	static ArrayList<String> name = new ArrayList<String>();
	static ArrayList<Texture2D> tex = new ArrayList<Texture2D>();

	public static void add(String name, Texture2D tex)
	{
		if (getTexture(name) == null)
		{
			AllItems.name.add(name);
			AllItems.tex.add(tex);
		}
	}

	public static Texture2D getTexture(String name)
	{
		for (int q = 0; q < AllItems.name.size(); q++)
		{
			if (AllItems.name.get(q).equals(name))
			{
				return tex.get(q);
			}
		}
		return null;
	}
}

class UseItem
{
	public String name; // objektin nimi jota k�ytet��n
	public String text = ""; // onnistuneen k�yt�n j�lkeen kirjoitetaan t�m� teksti
	public String remove = ""; // mik� poistetaan inventorysta
	public String newItem = ""; // jos saadaan joku tavara
}
