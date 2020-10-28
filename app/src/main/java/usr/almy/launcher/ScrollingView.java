package usr.almy.launcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressLint("ViewConstructor")
public class ScrollingView extends SurfaceView implements Runnable {
	Thread graphics;
	SurfaceHolder holder;
	Activity ctx;
	int min = 0;
	int max = 4;
	int current = 0;
	int visibleCurrent = 0;
	OnScrollListener onScrollListener = (v, pos) -> {
	};
	Paint paint = new Paint();
	Paint selected = new Paint();
	Rect selector = new Rect();
	Rect rect = new Rect();

	@SuppressLint("ClickableViewAccessibility")
	ScrollingView(Activity ctx) {
		super(ctx);

		this.ctx = ctx;
		holder = getHolder();

		setOnTouchListener((v, me) -> {
			synchronized (ScrollingView.this) {
				float y = me.getY();
				current = (int) (y / getHeight() * (max + 2));
				visibleCurrent = (int) (y - current * getHeight() / (max + 2));
				selector.set(
						0,
						current * getHeight() / (max + 2) + visibleCurrent,
						getWidth(),
						(current + 1) * getHeight() / (max + 2) + visibleCurrent
				);
				ScrollingView.this.notify();
				onScrollListener.onScroll(this, current);
			}
			return true;
		});

		setColor(0xff5e005e);

		graphics = new Thread(this);
		graphics.start();
	}

	public void setColor(int color) {
		this.paint.setColor(color);
		this.selected.setColor(color | 0xff773377);
	}

	@Override
	public void run() {
		//noinspection InfiniteLoopStatement
		while (true) {
			if (!holder.getSurface().isValid())
				continue;
			Canvas c = holder.lockCanvas();

			rect.set(0, 0, getWidth(), getHeight());
			c.drawRect(rect, paint);
			c.drawRect(selector, selected);

			holder.unlockCanvasAndPost(c);

			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	interface OnScrollListener {
		void onScroll(ScrollingView v, int pos);
	}
}
