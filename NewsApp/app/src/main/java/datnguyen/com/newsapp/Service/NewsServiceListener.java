package datnguyen.com.newsapp.Service;

import java.util.ArrayList;

import datnguyen.com.newsapp.Model.CustomError;
import datnguyen.com.newsapp.Model.News;

/**
 * Created by datnguyen on 12/23/16.
 */

public interface NewsServiceListener {

	/**
	 * callback when news service finishs loading list of news. Receiver will update UI properly
	 * @param newsList list of news returns from Search request
	 * @param totalItems total items of search query, used to check if loadmore is necessary
	 * @param currentPage current page of search result, used to load more next page
	 */
	void onNewsReceived(ArrayList<News> newsList, int totalItems, int currentPage);

	/**
	 * callback when news service failed to get news
	 * @param error error describing failure, containing errorId and errorMessage
	 */
	void onErrorReceived(CustomError error);
}
