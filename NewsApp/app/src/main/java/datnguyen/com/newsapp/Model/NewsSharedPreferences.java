package datnguyen.com.newsapp.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.Set;



/**
 * Created by datnguyen on 12/26/16.
 */

public class NewsSharedPreferences {

	private static NewsSharedPreferences sharedInstance;
	private SharedPreferences mSharedPreferences;

	public NewsSharedPreferences(Context context) {
		super();
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static NewsSharedPreferences getSharedPrefrences(Context context) {
		synchronized (NewsSharedPreferences.class) {
			if (sharedInstance == null) {
				sharedInstance = new NewsSharedPreferences(context);
			}
		}

		return sharedInstance;
	}


}
