package usr.almy.launcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.LinkedList;

import static usr.almy.launcher.Functional.getResizedBitmap;

@SuppressLint("ViewConstructor")
public class DesktopSurface extends SurfaceView implements Runnable {
	public static boolean updates = true;
	@SuppressLint("StaticFieldLeak")
	static Context s_ctx;
	Thread graphics;
	SurfaceHolder holder;
	Activity ctx;
	ArrayList<DesktopFeatures.App> all = new ArrayList<>();
	LinkedList<DesktopFeatures.DrawableObject> dynamicObjects = new LinkedList<>();
	DesktopFeatures.Cursor cursor;
	Paint paint = new Paint();
	DesktopFeatures.Background background;

	@SuppressLint("ClickableViewAccessibility")
	public DesktopSurface(Activity context) {
		super(context);
		holder = getHolder();
		ctx = context;
		s_ctx = context;

		background = new DesktopFeatures.Background(ctx);
		cursor = new DesktopFeatures.Cursor(context);
		cursor.x = 1440;
		cursor.setBitmap(getResizedBitmap(
				BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cursor), 64, 64
		));
		setOnTouchListener((v, me) -> {
			cursor.setPosition(me.getX(), me.getY());
			Log.i("Click, stack", DataShare.stack.toString());
			if (DataShare.stack.size() > 0)
				if (DataShare.stack.peek().equals("move")) {
					DataShare.stack.pop();
					String pkg = DataShare.stack.pop();
					DataShare.stack.clear();
					DataShare.stack.push(pkg);
					DataShare.stack.push("move");
					DesktopFeatures.App app = null;
					for (DesktopFeatures.App appl : all)
						if (appl.app_pkg.equals(pkg))
							app = appl;
					Log.i("Click, appmove", String.valueOf(app));
					if (app != null) {
						app.setPosition(me.getX(), me.getY());
						if (app.menu.getClass() != DesktopFeatures.Empty.class)
							app.menu = DesktopFeatures.Empty.empty;
					}
				}
			synchronized (DesktopSurface.this) {
				DesktopSurface.this.notify();
			}
			return true;
		});

		graphics = new Thread(this);
		graphics.start();

		refresh();
	}

	@Override
	public void run() {
		//noinspection InfiniteLoopStatement
		while (true) {
			long frameStart = System.currentTimeMillis();

			if (updates) {
				if (!holder.getSurface().isValid())
					continue;

				Canvas c = holder.lockCanvas();

				background.draw(c, paint);

				for (DesktopFeatures.DrawableObject d_o : all)
					d_o.draw(c, paint);

				for (DesktopFeatures.DrawableObject d_o : dynamicObjects)
					d_o.draw(c, paint);

				background.afterDraw(c, paint);

				cursor.draw(c, paint);

				holder.unlockCanvasAndPost(c);

                /*
                collisions = false;
                for (DesktopFeatures.DrawableObject dr_obj : all) {
                    collisions = collisions || dr_obj.checkCursorCollision(cursor);
                }
                */

				updates = false;

				for (DesktopFeatures.DrawableObject dr_obj : all)
					updates = updates || dr_obj.checkUpdates();
				for (DesktopFeatures.DrawableObject dr_obj : dynamicObjects)
					updates = updates || dr_obj.checkUpdates();
			}

			updates = updates || cursor.checkUpdates();

			if (!updates)
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}

			long frameEnd = System.currentTimeMillis();

			Log.i("Frame_Time", String.format("\n%d ms; updates: %b", frameEnd - frameStart, updates));
		}
	}

	@SuppressLint("SetTextI18n")
	public void start(String scn) {

	}

	public void parseDesktopCode(String re) {
		String[] arr = re.split(" ");
		for (int ip = 0; ip < arr.length; ip++) {
			String op = arr[ip];
			if (op.equals("paste")) {
				//pasting...
			} else if (op.equals("new")) {
				ip++;
				op = arr[ip];
				if (op.equals("applnk")) {
					Intent intent = new Intent(ctx, NewAppActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					DataShare.stack.push("applnk");
					ctx.startActivityForResult(intent, 100);
				}
			} else if (op.equals("move")) {
				String pkg = DataShare.stack.pop();
				DataShare.stack.clear();
				DataShare.stack.push(pkg);
				DataShare.stack.push("move");
			} else if (op.equals("del")) {
				String pkg = DataShare.stack.pop();
				DataShare.stack.clear();
				DesktopFeatures.App app = new DesktopFeatures.App("usr.almy.launcher", ctx);
				for (DesktopFeatures.App appl : all)
					if (appl.app_pkg.equals(pkg))
						app = appl;
				Log.i("Delete_applnk", app.file);
				FileUtil.deleteFile(app.file);
				refresh();
			}
		}
	}

	public void onMouseClick(DesktopFeatures.MouseEvent event) {
		boolean on_desktop = true;
		String re = null;
		if (DataShare.stack.size() > 0)
			if (DataShare.stack.peek().equals("move")) {
				DataShare.stack.pop();
				String pkg = DataShare.stack.pop();
				DataShare.stack.clear();
				DesktopFeatures.App app = null;
				for (DesktopFeatures.App appl : all)
					if (appl.app_pkg.equals(pkg))
						app = appl;
				if (app != null) {
					if (app.menu.getClass() != DesktopFeatures.Empty.class)
						app.menu = DesktopFeatures.Empty.empty;
				}
				FileUtil.writeFile(FileUtil.getPackageDataDir(ctx) + "/" + pkg, "link\n" + pkg + "\n" + cursor.x + ":" + cursor.y);
				refresh();

			}
		if (background.checkCursorCollision(cursor)) {
			Log.i("Click, bg", "bg");
			re = background.onClick(event, cursor);
		} else {
			for (DesktopFeatures.DrawableObject dr_obj : all) {
				if (dr_obj.checkCursorCollision(cursor)) {
					on_desktop = false;
					Log.i("Click", "non-desktop!");
					Log.i("Click", ((DesktopFeatures.App) dr_obj).file);
					re = dr_obj.onClick(event, cursor);
					//действия с дровэблем
				} else
					dr_obj.isActivated = false;
			}
			if (on_desktop) {
				Log.i("Click, bg", "bg");
				for (DesktopFeatures.DrawableObject eve : all)
					eve.deactivate();
				Log.i("puts", "#");
				re = background.onClick(event, cursor);
			}
		}
		if (re == null)
			re = "";
		Log.d("code", re);
		parseDesktopCode(re);
		updates = true;
		synchronized (this) {
			notify();
		}
	}

	public void refresh() {
		FileUtil.deleteFile(FileUtil.getPackageDataDir(ctx) + "/applnk");
		ArrayList<String> files = new ArrayList<>();
		FileUtil.listDir(FileUtil.getPackageDataDir(ctx), files);
		all.clear();
		for (String file : files) {
			if (FileUtil.isDirectory(file)) {

			} else {
				String[] data = FileUtil.readFile(file).split("\n");
				if (data[0].equals("link"))
					if (all.size() < 10) {
						DesktopFeatures.App app = new DesktopFeatures.App(data[1], ctx);
						app.file = file;
						app.setPosition(Float.parseFloat(data[2].split(":")[0]),
								Float.parseFloat(data[2].split(":")[1]));
						all.add(app);
					}
			}
		}
		synchronized (DesktopSurface.this) {
			DesktopSurface.this.notify();
		}
	}
}