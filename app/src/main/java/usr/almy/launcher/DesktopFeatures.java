package usr.almy.launcher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.LinkedList;

import static usr.almy.launcher.Functional.getResizedBitmap;

public class DesktopFeatures {
	static Paint main_paint = new Paint();
	static Paint main_paint_anti_alias = new Paint();
	static Paint main_paint_anti_alias_big = new Paint();

	static {
		main_paint.setAntiAlias(false);
		main_paint_anti_alias.setAntiAlias(true);
		main_paint_anti_alias.setTypeface(Typeface.createFromAsset(DesktopSurface.s_ctx.getAssets(), "SourceCodePro.ttf"));
		main_paint_anti_alias.setTextSize(16f);
		main_paint_anti_alias.setColor(Color.WHITE);

		main_paint_anti_alias_big.setAntiAlias(true);
		main_paint_anti_alias_big.setTypeface(Typeface.createFromAsset(DesktopSurface.s_ctx.getAssets(), "Asinastra.ttf"));
		main_paint_anti_alias_big.setTextSize(24f);
		main_paint_anti_alias_big.setColor(Color.WHITE);
	}

	enum MouseEvent {LEFT, RIGHT}

	public static abstract class DrawableObject {
		float x, y;
		Context ctx;
		boolean isActivated;

		public DrawableObject(Context ctx) {
			this.ctx = ctx;
		}

		public void setPosition(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public abstract void draw(Canvas cv, Paint paint);

		public abstract boolean checkCursorCollision(Cursor cursor);

		public abstract String onClick(MouseEvent event, Cursor c);

		public abstract boolean checkUpdates();

		public abstract void deactivate();

		Context getContext() {
			return ctx;
		}
	}

	public static class Cursor extends DrawableObject {
		Bitmap crs;
		boolean relocation = true;

		public Cursor(Context ctx) {
			super(ctx);
		}

		public void setBitmap(Bitmap bm) {
			crs = bm;
		}

		@Override
		public void draw(Canvas cv, Paint paint) {
			cv.drawBitmap(crs, x, y, paint);
		}

		@Override
		public void setPosition(float x, float y) {
			relocation = true;
			super.setPosition(x, y);
		}

		@Override
		public boolean checkCursorCollision(Cursor cursor) {
			return false;
		}

		@Override
		public String onClick(MouseEvent event, Cursor c) {
			return null;
		}

		@Override
		public boolean checkUpdates() {
			if (relocation) {
				relocation = false;
				return true;
			}
			return false;
		}

		@Override
		public void deactivate() {

		}
	}

	public static class Empty extends DrawableObject {
		@SuppressLint("StaticFieldLeak")
		public static Empty empty;

		public Empty(Context ctx) {
			super(ctx);
		}

		public static void load(Context ctx) {
			empty = new Empty(ctx);
		}

		@Override
		public void draw(Canvas cv, Paint paint) {
		}

		@Override
		public boolean checkCursorCollision(Cursor cursor) {
			return false;
		}

		@Override
		public String onClick(MouseEvent event, Cursor c) {
			return null;
		}

		@Override
		public boolean checkUpdates() {
			return false;
		}

		@Override
		public void deactivate() {

		}
	}

	public static class App extends DrawableObject {
		static float[] cmdt = {
				1, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 1, 0, 0,
				0, 0, 0, 1, 0,
		};
		public DrawableObject menu;
		String file;
		String app_pkg;
		RectF rect = new RectF();
		ApplicationInfo app;
		Drawable icon;
		String name;
		float drawTextX, drawTextY;
		private Bitmap app_bg;

		App(String pkg, Context ctx) {
			super(ctx);
			try {
				menu = Empty.empty;

				app_bg = getResizedBitmap(
						BitmapFactory.decodeResource(ctx.getResources(), R.drawable.appbg), 175, 175
				);
				app_pkg = pkg;
				rect.set(x, y, x + 170, y + 170);
				try {
					app = getContext().getPackageManager().getApplicationInfo(app_pkg, 0);
				} catch (PackageManager.NameNotFoundException e) {
					e.printStackTrace();
				}

				name = (String) getContext().getPackageManager().getApplicationLabel(app);
				icon = getContext().getPackageManager().getApplicationIcon(app);

				ColorMatrix cm = new ColorMatrix();
				cm.set(cmdt);
				ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
				icon.setColorFilter(filter);

				if (name.length() > getContext().getString(R.string.app_name).length()) {
					name = name.substring(0, getContext().getString(R.string.app_name).length() - 3) + "…";
				}

				icon.setBounds((int) x + 10, (int) y, (int) x + 160, (int) y + 150);

				if (name.length() >= getContext().getString(R.string.app_name).length())
					drawTextX = x;
				else
					drawTextX = x + 4 * (getContext().getString(R.string.app_name).length() - name.length());
				drawTextY = y + 162;
				drawTextX += 12;
			} catch (Exception ignored) {
			}
		}

		@Override
		public void setPosition(float x, float y) {
			try {
				rect.set(x, y, x + 170, y + 170);
				icon.setBounds((int) x + 12, (int) y + 2, (int) x + 158, (int) y + 148);
				if (name.length() >= getContext().getString(R.string.app_name).length())
					drawTextX = x;
				else
					drawTextX = x + 4 * (getContext().getString(R.string.app_name).length() - name.length());
				drawTextY = y + 162;
				drawTextX += 12;
			} catch (Exception ignored) {
			}
			super.setPosition(x, y);
		}

		@Override
		public void draw(Canvas cv, Paint paint) {
			if ((Object) icon == (Object) name)
				return;
			cv.drawBitmap(app_bg, x, y, paint);
			icon.draw(cv);
			cv.drawText(name, drawTextX, drawTextY, main_paint_anti_alias);
			menu.draw(cv, paint);
		}

		@Override
		public boolean checkCursorCollision(Cursor cursor) {
			return rect.contains(cursor.x, cursor.y) || menu.checkCursorCollision(cursor);
		}

		@Override
		public String onClick(MouseEvent event, Cursor c) {
			if (event == MouseEvent.LEFT) {
				if (menu.checkCursorCollision(c)) {
					DataShare.stack.push(app_pkg);
					return menu.onClick(event, c);
				} else if (isActivated) {
					if (!app_pkg.equals("usr.almy.launcher")) {
						Intent executor = getContext().getPackageManager().getLaunchIntentForPackage(app_pkg);
						if (executor != null) {
							try {
								getContext().startActivity(executor);
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(getContext(), "An Error Occured!", Toast.LENGTH_LONG).show();
							}
						}
					}//вызов приложения (это дабл клик)
				} else {
					isActivated = true;
				}
			} else {
				menu = new Menu(ctx, new LinkedList<>(Arrays.asList(
						"Move", "Delete"
				)), new LinkedList<>(Arrays.asList(
						"move", "del"
				)));
				menu.setPosition(c.x, c.y);
			}
			return null;
		}

		@Override
		public boolean checkUpdates() {
			return false;
		}

		@Override
		public void deactivate() {
			menu = Empty.empty;
		}
	}

	public static class Background extends DrawableObject {
		Bitmap bg;
		DrawableObject menu = Empty.empty;
		boolean update;

		public Background(Context ctx) {
			super(ctx);
			bg = getResizedBitmap(
					BitmapFactory.decodeResource(ctx.getResources(), R.drawable.bg), 1050, 720
			);
		}

		@Override
		public void draw(Canvas cv, Paint paint) {
			cv.drawBitmap(bg, 0, 0, paint);
			if (update)
				update = false;
		}

		public void afterDraw(Canvas cv, Paint paint) {
			menu.draw(cv, paint);
		}

		@Override
		public boolean checkCursorCollision(Cursor cursor) {
			try {
				Log.i("Desktop collision", String.format("%s: %s !: (%s, %s)", menu.checkCursorCollision(cursor), ((Menu) menu).layout.toString(), cursor.x, cursor.y));
			} catch (Exception ignored) {
			}
			return menu.checkCursorCollision(cursor);
		}

		@Override
		public String onClick(MouseEvent event, Cursor c) {
			if (event == DesktopFeatures.MouseEvent.RIGHT) {
				Log.i("Click, bg", "right");
				menu = new DesktopFeatures.Menu(ctx, new LinkedList<>(
						Arrays.asList("New", "Paste", "Settings")
				), new LinkedList<>(
						Arrays.asList("desktop.create", "paste", "open settings")
				));
				menu.setPosition(c.x, c.y);
				update = true;
			} else {
				Log.i("Click, bg", "left");
				if (menu.checkCursorCollision(c)) {
					String re = menu.onClick(event, c);
					if (re.equals("desktop.create")) {
						float mx = menu.x;
						float my = menu.y;
						menu = new DesktopFeatures.Menu(ctx, new LinkedList<>(
								Arrays.asList("Application", "Folder")
						), new LinkedList<>(
								Arrays.asList("new applnk", "new folder")
						));
						menu.setPosition(mx, my);
						return null;
					}
					return re;
				} else if (menu.getClass() == Menu.class)
					menu = Empty.empty;
				Log.i("Desktop", "Clicked!");
			}
			return null;
		}

		@Override
		public boolean checkUpdates() {
			return update;
		}

		@Override
		public void deactivate() {

		}
	}

	public static class Menu extends DrawableObject {
		public static Bitmap up, mid, down;
		LinkedList<String> data;
		LinkedList<String> actions;
		Direction direction = Direction.DOWN_RIGHT;
		Rect layout = new Rect();
		Bitmap todraw;
		String first;
		String last;

		public Menu(Context ctx, LinkedList<String> ll, LinkedList<String> actions) {
			super(ctx);
			this.data = ll;
			this.actions = actions;
			if (up == null) {
				up = getResizedBitmap(
						BitmapFactory.decodeResource(ctx.getResources(), R.drawable.menu_up), 240, 60
				);
				mid = getResizedBitmap(
						BitmapFactory.decodeResource(ctx.getResources(), R.drawable.menu_middle), 240, 60
				);
				down = getResizedBitmap(
						BitmapFactory.decodeResource(ctx.getResources(), R.drawable.menu_down), 240, 60
				);
			}
			first = data.getFirst();
			last = data.getLast();
		}

		@SuppressWarnings("StringEquality")
		@Override
		public void draw(Canvas cv, Paint paint) {
			float dx, dy;
			dx = x;
			dy = y;
			if (direction == Direction.DOWN_LEFT || direction == Direction.UP_LEFT)
				dx -= 240;

			if (direction == Direction.UP_RIGHT || direction == Direction.UP_LEFT)
				dy -= data.size() * 60;

			for (String s : data) {
				if (first == s)
					todraw = up;
				else if (last == s)
					todraw = down;
				else
					todraw = mid;

				cv.drawBitmap(todraw, dx, dy, paint);
				cv.drawText(s.split("\\|")[0], dx + 30, dy + 45, main_paint_anti_alias_big);

				dy += 60;
			}

			//cv.drawRect(layout, new Paint(Color.GREEN));
			//Log.d("Desktop L-T", layout.toString());
		}

		@Override
		public boolean checkCursorCollision(Cursor cursor) {
			return layout.contains((int) cursor.x, (int) cursor.y);
		}

		@Override
		public String onClick(MouseEvent event, Cursor c) {
			return actions.get(((int) c.y - (int) layout.top) / 60);
		}

		public void setData(LinkedList<String> data) {
			this.data = data;
			first = data.getFirst();
			last = data.getLast();
			switch (direction) {
				case UP_LEFT:
					layout.set((int) x - 240, (int) y - data.size() * 60, (int) x, (int) y);
					break;
				case UP_RIGHT:
					layout.set((int) x, (int) y - data.size() * 60, (int) x + 240, (int) y);
					break;
				case DOWN_LEFT:
					layout.set((int) x - 240, (int) y, (int) x, (int) y + data.size() * 60);
					break;
				case DOWN_RIGHT:
					layout.set((int) x, (int) y, (int) x + 240, (int) y + data.size() * 60);
					break;
			}
		}

		@Override
		public void setPosition(float x, float y) {
			if ((int) x / 525 < 1) {
				if ((int) y / 360 < 1)
					direction = Direction.DOWN_RIGHT;
				else
					direction = Direction.UP_RIGHT;
			} else {
				if ((int) y / 360 < 1)
					direction = Direction.DOWN_LEFT;
				else
					direction = Direction.UP_LEFT;
			}
			switch (direction) {
				case UP_LEFT:
					layout.set((int) x - 240, (int) y - data.size() * 60, (int) x, (int) y);
					break;
				case UP_RIGHT:
					layout.set((int) x, (int) y - data.size() * 60, (int) x + 240, (int) y);
					break;
				case DOWN_LEFT:
					layout.set((int) x - 240, (int) y, (int) x, (int) y + data.size() * 60);
					break;
				case DOWN_RIGHT:
					layout.set((int) x, (int) y, (int) x + 240, (int) y + data.size() * 60);
					break;
			}
			super.setPosition(x, y);
		}

		@Override
		public boolean checkUpdates() {
			return false;
		}

		@Override
		public void deactivate() {

		}

		private enum Direction {
			UP_RIGHT, DOWN_RIGHT, UP_LEFT, DOWN_LEFT
		}
	}
}
