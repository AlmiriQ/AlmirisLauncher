package usr.almy.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;


public class HomeScreen extends Activity {
	LinearLayout desktop_layout;
	DesktopSurface desktop;
	LinearLayout up_kp, down_kp;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 100) {
			desktop.background.menu = new DesktopFeatures.Empty(this);
			FileUtil.writeFile(FileUtil.getPackageDataDir(this) + "/" + DataShare.stack.peek(), "link\n" + DataShare.stack.peek() + "\n" + desktop.cursor.x + ":" + desktop.cursor.y);
			DataShare.stack.clear();
			desktop.refresh();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		DataShare.load(this);
		DesktopFeatures.Empty.load(this);
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.home);

		desktop_layout = findViewById(R.id.desktop);
		desktop = new DesktopSurface(this);
		up_kp = findViewById(R.id.up_key_part);
		down_kp = findViewById(R.id.down_key_part);

		desktop_layout.setBackgroundColor(Color.GREEN);
		up_kp.setBackgroundColor(Color.BLUE);
		down_kp.setBackgroundColor(Color.RED);

		down_kp.setOnLongClickListener(v -> {
			Intent executor = getPackageManager().getLaunchIntentForPackage("com.android.settings");
			if (executor != null) {
				try {
					startActivity(executor);
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(HomeScreen.this, "An Error Occurred!", Toast.LENGTH_LONG).show();
				}
			}
			return false;
		});

		desktop_layout.addView(desktop, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			//Toast.makeText(this, "Left Click!", Toast.LENGTH_LONG).show();
			desktop.onMouseClick(DesktopFeatures.MouseEvent.LEFT);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			//Toast.makeText(this, "Right Click!",Toast.LENGTH_LONG).show();
			desktop.onMouseClick(DesktopFeatures.MouseEvent.RIGHT);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		DesktopSurface.updates = true;
		//noinspection SynchronizeOnNonFinalField
		synchronized (desktop) {
			desktop.notify();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
