package com.mjt.reino;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;

public class Util
{
	private static boolean DEBUG = true;
	private static boolean WRITE_LOG = true;
	private static boolean DESKTOP = Gdx.app.getType() == ApplicationType.Desktop;

	public static void println(String txt)
	{
		if (DEBUG && DESKTOP)
			System.out.println("DEBUG: " + txt);

		if (WRITE_LOG && DESKTOP)
		{
			// TODO
		}
	}

	public static void quickSort(int array[])
	{
		quickSort(array, 0, array.length - 1);
	}

	private static void quickSort(int array[], int low, int n)
	{
		int lo = low;
		int hi = n;
		if (lo >= n)
		{
			return;
		}
		int mid = array[(lo + hi) / 2];
		while (lo < hi)
		{
			while (lo < hi && array[lo] < mid)
			{
				lo++;
			}
			while (lo < hi && array[hi] > mid)
			{
				hi--;
			}
			if (lo < hi)
			{
				int T = array[lo];
				array[lo] = array[hi];
				array[hi] = T;
			}
		}
		if (hi < lo)
		{
			int T = hi;
			hi = lo;
			lo = T;
		}
		quickSort(array, low, lo);
		quickSort(array, lo == low ? lo + 1 : lo, n);
	}

	public static void updateMouse()
	{
		int MX = Gdx.input.getX(), MY = Gdx.input.getY();
		if (Gdx.input.getX() < 0)
			MX = 0;
		if (Gdx.input.getY() < 0)
			MY = 0;
		if (Gdx.input.getX() > Gdx.graphics.getWidth())
			MX = Gdx.graphics.getWidth();
		if (Gdx.input.getY() > Gdx.graphics.getHeight())
			MY = Gdx.graphics.getHeight();
		
		Gdx.input.setCursorPosition(MX, MY);
	}
}
