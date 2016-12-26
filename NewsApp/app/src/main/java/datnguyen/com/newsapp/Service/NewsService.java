package datnguyen.com.newsapp.Service;

import android.app.LoaderManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import static datnguyen.com.newsapp.Constants.URL_PARAM_APIKEY;
import static datnguyen.com.newsapp.Constants.URL_PARAM_ORDER_BY;
import static datnguyen.com.newsapp.Constants.URL_PARAM_PAGE_SIZE;
import static datnguyen.com.newsapp.Constants.URL_PARAM_SHOW_FIELDS;
import static datnguyen.com.newsapp.Constants.URL_PARAM_SHOW_FIELDS_VALUES;

/**
 * Created by datnguyen on 12/22/16.
 */

public final class NewsService {

	private final static String TAG_NAME = NewsService.class.getSimpleName();
	private final static String METHOD_GET = "GET";
	private final static String RESPONE_OK = "ok";

	private static NewsService sharedInstance;
	private NewsServiceListener serviceListener;

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

	public static NewsService getNewsService() {

		synchronized (NewsService.class) {
			if (sharedInstance == null) {
				sharedInstance = new NewsService();
			}
		}

		return sharedInstance;
	}


	// Hide default constructor, only allow access via getNewsService() method
	private NewsService() {

	}

	public void startSearch(final String keyword, final HashMap searchParams) {

		// create async task
		AsyncTask asyncTask = new AsyncTask() {
			@Override
			protected Object doInBackground(Object[] objects) {

				Uri searchUri = createURL(Constants.URL_BASE, searchParams);
				Log.v(TAG_NAME, "searchURL: " + searchUri.toString());
				String jsonResponse = "";
				try {
					jsonResponse = makeHttpRequest(searchUri);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return jsonResponse;
			}

			@Override
			protected void onPostExecute(Object jsonString) {
				super.onPostExecute(jsonString);

				CustomError error = null;
				try {
					JSONObject rootObject = new JSONObject((String) jsonString);
					JSONObject responseObject = rootObject.optJSONObject("response");

					String messageObject = rootObject.optString("message");
					if (!TextUtils.isEmpty(messageObject)) {
						error = new CustomError();
						error.setErrorMessage(messageObject);
						error.setErrorId(CustomError.ERROR_SERVER_MESSAGE);
					} else {

						// get result
						String statusObject = responseObject.optString("status");
						if (!statusObject.equals(RESPONE_OK)) {
							error = new CustomError();
							error.setErrorId(CustomError.ERROR_RESPONSE_NOT_OK);
						} else {
							// get total items, for check loadmore purpose
							int totalItems = responseObject.optInt("total");
							int currentPage = responseObject.optInt("currentPage");

							JSONArray jsonArray = responseObject.optJSONArray("results");
							ArrayList<News> listNewss = parseNewsFromJSONArray(jsonArray);

							// send result to delegate (listener)
							if (getNewsService().getServiceListener() != null) {
								getNewsService().getServiceListener().onNewsReceived(listNewss, totalItems, currentPage);
							}
						}
					}

				} catch (JSONException e) {
					e.printStackTrace();
					error = new CustomError();
					error.setErrorId(CustomError.ERROR_EXCEPTION_PARSE);
				} catch (ParseException e) {
					e.printStackTrace();
					error = new CustomError();
					error.setErrorId(CustomError.ERROR_EXCEPTION_PARSE);
				} finally {
					if (error != null) {
						// send error to delegate (listener)
						if (getNewsService().getServiceListener() != null) {
							getNewsService().getServiceListener().onErrorReceived(error);
						}
					}
				}
			}
		};

		asyncTask.execute();

	}

	private Uri createURL(String endPoint, HashMap params) {

		Uri uri = Uri.parse(endPoint);
		Uri.Builder builder = uri.buildUpon();

		Iterator entries = params.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			String param = (String) entry.getKey();

			String valueString = "";
			Object value = entry.getValue();
			if (value instanceof String) {
				valueString = (String) value;
			} else if (value instanceof Date) {
				// parse date to string using format yyyy-MM-dd
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				valueString = (String) dateFormat.format((Date) value);
			} else {
				valueString = value.toString();
			}

			// add param to uri
			builder.appendQueryParameter(param, valueString);
		}

		Log.v("TAG", "final search build: " + builder.build().toString());

		return builder.build();
	}

	private String makeHttpRequest(Uri uri) throws IOException {
		String jsonResponse = "";
		HttpURLConnection httpURLConnection = null;
		InputStream inputStream = null;

		try {
			URL url = new URL(uri.toString());
			httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod(METHOD_GET);
			httpURLConnection.setConnectTimeout(CONNECTION_TIMEOUT);

			httpURLConnection.connect();

			// read to input stream
			inputStream = httpURLConnection.getInputStream();

			// download and decode string response using String Builder
			StringBuilder stringBuilder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}

			jsonResponse = stringBuilder.toString();

		} catch (IOException e) {

		} finally {
			// disconnect connection and close input stream
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
			}

			if (inputStream != null) {
				inputStream.close();
			}
		}

		return jsonResponse;
	}

	private ArrayList<News> parseNewsFromJSONArray(JSONArray jsonArray) throws ParseException {
		ArrayList<News> listNewss = new ArrayList<>();

		if (jsonArray == null) {
			return listNewss;
		}

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonNews = jsonArray.optJSONObject(i);

			// parse json News object to News object
			News newNews = new News();
			newNews.setNewsId(jsonNews.optString("id"));

			String sectionId = jsonNews.optString("sectionId");
			if (sectionId != null) {
				newNews.setSectionId(sectionId);
			}

			String sectionName = jsonNews.optString("sectionName");
			if (sectionName != null) {
				newNews.setSectionName(sectionName);
			}

			String webTitle = jsonNews.optString("webTitle");
			if (webTitle != null) {
				newNews.setTitle(webTitle);
			}

			String webUrl = jsonNews.optString("webUrl");
			if (webUrl != null) {
				newNews.setWebUrl(webUrl);
			}

			// date
			String strDate = jsonNews.optString("webPublicationDate");
			if (strDate != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
				Date date = (Date) dateFormat.parse(strDate);
				newNews.setPublicationDate(date);
			}

			JSONObject fields = jsonNews.optJSONObject("fields");
			if (fields != null) {
				String thumbnail = fields.optString("thumbnail");
				if (thumbnail != null) {
					newNews.setThumbnailUrl(thumbnail);
				}
			}

			listNewss.add(newNews);

		}

		return listNewss;
	}

	public HashMap baseParams() {
		HashMap hashMap = new HashMap();

		hashMap.put(URL_PARAM_APIKEY, "test");
		hashMap.put(URL_PARAM_PAGE_SIZE, Integer.valueOf(REQUEST_ITEMS_PER_PAGE));
		hashMap.put(URL_PARAM_SHOW_FIELDS, URL_PARAM_SHOW_FIELDS_VALUES);
		hashMap.put(URL_PARAM_ORDER_BY, orderByString());


		return hashMap;

	}

	public String orderByString() {
		return OrderType.RELEVANCE.typeValue;
	}

}
