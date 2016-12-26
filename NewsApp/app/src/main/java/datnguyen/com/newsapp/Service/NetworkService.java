package datnguyen.com.newsapp.Service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by datnguyen on 12/23/16.
 */

public class NetworkService {

	private static NetworkService sharedInstance = null;

	private Context mContext;

	private NetworkService() {
		// hide default constructor
	}

	public static NetworkService getNetworkService(Context context) {

		synchronized (NewsService.class) {
			if (sharedInstance == null) {
				sharedInstance = new NetworkService();
				sharedInstance.mContext = context;
			}
		}
		return sharedInstance;
	}

	/**
	 * Method to check if network is available
	 * @return true if network is available, otherwise false
	 */
	public Boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) sharedInstance.mContext.getSystemService (Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}
}
