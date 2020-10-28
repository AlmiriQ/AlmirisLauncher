package usr.almy.launcher;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

class ScrollApps {
	Activity activity;
	PackageManager pm;
	LinearLayout[] apps = new LinearLayout[10];
	TextView[] labels = new TextView[10];
	ImageView[] icons = new ImageView[10];
	int nowPos = 0;
	int maxPos = DataShare.list.size() - 9;

	ScrollApps(Activity activity, LinearLayout main) {
		this.activity = activity;
		pm = activity.getPackageManager();
		for (int i = nowPos; i < apps.length; i++) {
			apps[i] = new LinearLayout(activity);
			apps[i].setGravity(Gravity.CENTER);

			labels[i] = new TextView(activity);
			labels[i].setTypeface(Typeface.createFromAsset(activity.getAssets(), "Asinastra.ttf"));
			labels[i].setTextSize(16);
			labels[i].setText(DataShare.list.get(i).loadLabel(pm));
			icons[i] = new ImageView(activity);
			icons[i].setImageDrawable(DataShare.list.get(i).loadIcon(pm));

			main.addView(apps[i], new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 144));
			apps[i].addView(icons[i], new LinearLayout.LayoutParams(144, 144));
			apps[i].addView(labels[i], new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 144));

			final int finalI = i;
			apps[i].setOnClickListener(v -> {
				DataShare.stack.push(DataShare.list.get(finalI).packageName);
				activity.finishActivity(100);
			});
			apps[i].setOnLongClickListener(v -> {
				Toast.makeText(activity, DataShare.list.get(finalI).packageName, Toast.LENGTH_SHORT).show();
				return false;
			});
		}
	}

	void scrollTo(int index) {
		if ((index >= 0) && (index < maxPos))
			nowPos = index;
		else return;
		for (int i = nowPos, j = 0; j < apps.length; i++, j++) {
			labels[j].setText(DataShare.list.get(i).loadLabel(pm));
			icons[j].setImageDrawable(DataShare.list.get(i).loadIcon(pm));
			final int finalI = i;
			apps[j].setOnLongClickListener(v -> {
				Toast.makeText(activity, DataShare.list.get(finalI).packageName, Toast.LENGTH_SHORT).show();
				return false;
			});
		}
	}
}

public class NewAppActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_app);

		PackageManager pm = getPackageManager();

		LinearLayout ss = findViewById(R.id.side_scroll);
		ScrollingView sv = new ScrollingView(this);
		sv.max = DataShare.list.size();
		ss.addView(sv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		LinearLayout main = findViewById(R.id.apps);

		ScrollApps scrollApps = new ScrollApps(this, main);

		sv.max = DataShare.list.size();
		sv.onScrollListener = (v, pos) -> scrollApps.scrollTo(pos - 1);
	}
}