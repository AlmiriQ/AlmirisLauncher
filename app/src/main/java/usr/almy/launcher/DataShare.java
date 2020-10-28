package usr.almy.launcher;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class DataShare {
	public static Hashtable<String, String> hashtable = new Hashtable<>();
	public static Stack<String> stack = new Stack<>();
	public static LinkedList<ApplicationInfo> list = new LinkedList<>();

	private static boolean myFinder(LinkedList<ApplicationInfo> l, String pkg) {
		for (ApplicationInfo ai : l)
			if (ai.packageName.equals(pkg))
				return false;
		return true;
	}

	static void load(Context context) {
		PackageManager pm = context.getPackageManager();
		List<ApplicationInfo> list = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo every : list)
			if (myFinder(DataShare.list, every.packageName))
				if (!every.packageName.equals(pm.getApplicationLabel(every).toString()))
					if (pm.getLaunchIntentForPackage(every.packageName) != null)
						DataShare.list.add(every);
		DataShare.list.sort((o1, o2) -> pm.getApplicationLabel(o1).toString().compareTo(pm.getApplicationLabel(o2).toString()));
		Log.i("Data_len", String.valueOf(DataShare.list.size()));
	}
}