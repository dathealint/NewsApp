package datnguyen.com.newsapp.Service;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import datnguyen.com.newsapp.Constants;
import datnguyen.com.newsapp.Model.CustomError;
import datnguyen.com.newsapp.Model.News;

import static datnguyen.com.newsapp.Constants.CONNECTION_TIMEOUT;
import static datnguyen.com.newsapp.Constants.REQUEST_ITEMS_PER_PAGE;
import static datnguyen.com.newsapp.Constants.RESPONE_OK;
import static datnguyen.com.newsapp.Constants.URL_PARAM_APIKEY;
import static datnguyen.com.newsapp.Constants.URL_PARAM_ORDER_BY;
import static datnguyen.com.newsapp.Constants.URL_PARAM_PAGE_SIZE;
import static datnguyen.com.newsapp.Constants.URL_PARAM_SHOW_FIELDS;
import static datnguyen.com.newsapp.Constants.URL_PARAM_SHOW_FIELDS_VALUES;
import static datnguyen.com.newsapp.Service.NewsLoader.BUNDLE_LOADER_PARAMS_KEY;
import static datnguyen.com.newsapp.Service.NewsLoader.KEY_CURRENT_PAGE;
import static datnguyen.com.newsapp.Service.NewsLoader.KEY_LIST;
import static datnguyen.com.newsapp.Service.NewsLoader.KEY_TOTAL_ITEMS;
import static datnguyen.com.newsapp.Service.NewsLoader.TYPE_SEARCH;

/**
 * Created by datnguyen on 12/22/16.
 */

public final class NewsService implements LoaderManager.LoaderCallbacks {

	private final static String TAG_NAME = NewsService.class.getSimpleName();


	private static NewsService sharedInstance;
	private NewsServiceListener serviceListener;
	private Context mContext;

	public enum OrderType {
		NEWEST("newest"),
		RELEVANCE("relevance");

		private String typeValue;

		OrderType(String type) {
			this.typeValue = type;
		}

		public static OrderType getTypeEnumByStringValue(String typeValue) {
			for (OrderType type : values()) {
				if (type.typeValue.equalsIgnoreCase(typeValue)) {
					return type;
				}
			}
			return RELEVANCE;
		}
	}

	public void setServiceListener(NewsServiceListener serviceListener) {
		this.serviceListener = serviceListener;
	}

	public NewsServiceListener getServiceListener() {
		return serviceListener;
	}

	public static NewsService getNewsService(Context context) {
		synchronized (NewsService.class) {
			if (sharedInstance == null) {
				sharedInstance = new NewsService(context);
			}
		}
		return sharedInstance;
	}


	// Hide default constructor, only allow access via getNewsService() method
	private NewsService(Context context) {
		this.mContext = context;
	}

	public void startSearch(HashMap searchParams) {
		LoaderManager loaderManager = ((Activity) this.mContext).getLoaderManager();

		// check if there's another request already loading
		NewsLoader loader = (NewsLoader)loaderManager.getLoader(TYPE_SEARCH);
		if (loader != null) {
			loaderManager.destroyLoader(TYPE_SEARCH);
		}

		Bundle bundle = new Bundle();
		bundle.putSerializable(BUNDLE_LOADER_PARAMS_KEY, searchParams);
		loader = (NewsLoader) loaderManager.initLoader(TYPE_SEARCH, bundle, this);
		loader.forceLoad();
	}

	/**
	 * Base params containing all basic params for story request
	 * @return hashmap of all basic params
	 */
	public HashMap baseParams() {
		HashMap hashMap = new HashMap();
		hashMap.put(URL_PARAM_APIKEY, "test");
		hashMap.put(URL_PARAM_PAGE_SIZE, Integer.valueOf(REQUEST_ITEMS_PER_PAGE));
		hashMap.put(URL_PARAM_SHOW_FIELDS, URL_PARAM_SHOW_FIELDS_VALUES);
		hashMap.put(URL_PARAM_ORDER_BY, orderByString());

		return hashMap;
	}

	/**
	 * @return return orderBy value as String
	 */
	public String orderByString() {
		return OrderType.RELEVANCE.typeValue;
	}

	/* Callbacks implementation of LoaderManager */
	@Override
	public Loader onCreateLoader(int i, Bundle bundle) {
		return new NewsLoader(mContext, bundle);
	}

	@Override
	public void onLoadFinished(Loader loader, Object object) {
		// parse info for Search loader
		if (loader.getId() == TYPE_SEARCH) {
			if (object instanceof CustomError) {
				// send error to delegate (listener)
				if (NewsService.getNewsService(mContext).getServiceListener() != null) {
					NewsService.getNewsService(mContext).getServiceListener().onErrorReceived((CustomError)object);
				}
			} else if (object instanceof HashMap) {
				HashMap params = (HashMap)object;
				ArrayList<News> list = (ArrayList)params.get(KEY_LIST);
				int totalItems = (int)params.get(KEY_TOTAL_ITEMS);
				int currentPage = (int)params.get(KEY_CURRENT_PAGE);

				// send result to delegate (listener)
				if (NewsService.getNewsService(mContext).getServiceListener() != null) {
					NewsService.getNewsService(mContext).getServiceListener().onNewsReceived(list, totalItems, currentPage);
				}
			}
		}
	}

	@Override
	public void onLoaderReset(Loader loader) {

	}
}
