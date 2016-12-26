package datnguyen.com.newsapp;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import datnguyen.com.newsapp.Model.CustomError;
import datnguyen.com.newsapp.Model.News;
import datnguyen.com.newsapp.Service.LoadmoreInterface;
import datnguyen.com.newsapp.Service.NetworkService;
import datnguyen.com.newsapp.Service.NewsService;
import datnguyen.com.newsapp.Service.NewsServiceListener;

import static datnguyen.com.newsapp.Constants.DEFAULT_CURRENT_PAGE;


public class MainActivity extends AppCompatActivity {

	private SearchView searchView = null;
	private RecyclerView recycleView = null;
	private TextView tvErrorMessage = null;

	private NewsAdapter newsAdapter = null;
	private ArrayList<News> newsList = new ArrayList<>();

	private String currentKeyword = "";
	private int totalItems = 0;
	private int currentPage = DEFAULT_CURRENT_PAGE;

	NewsServiceListener newsServiceListener = null;
	private static MainActivity mSharedInstance;

	public static MainActivity getSharedInstance() {
		return mSharedInstance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSharedInstance = this;
		// grab controls
		searchView = (SearchView) findViewById(R.id.searchView);
		recycleView = (RecyclerView) findViewById(R.id.recycleView);
		tvErrorMessage = (TextView) findViewById(R.id.tvErrorMessage);
		tvErrorMessage.setVisibility(View.GONE);

		LoadmoreInterface loadmoreInterface = new LoadmoreInterface() {
			@Override
			public void onLoadmoreBegin() {
				// search loadmore, result will be added to current list
				startSearch(currentKeyword);
			}

			@Override
			public void onLoadmoreCompleted() {

			}
		};

		newsAdapter = new NewsAdapter(newsList, loadmoreInterface);

		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
		recycleView.setLayoutManager(mLayoutManager);
		recycleView.setItemAnimator(new DefaultItemAnimator());
		recycleView.setAdapter(newsAdapter);

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String s) {
				// clear current search result for a fresh search
				newsList.clear();
				totalItems = 0;
				currentKeyword = "";
				currentPage = DEFAULT_CURRENT_PAGE;

				newsAdapter.notifyDataSetChanged();

				// hide error message if showing
				tvErrorMessage.setText("");
				newsAdapter.setFooterEnabled(true);

				recycleView.setVisibility(View.VISIBLE);
				tvErrorMessage.setVisibility(View.GONE);

				startSearch(s);
				searchView.clearFocus();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				return false;
			}
		});

		searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {
				Log.v("MAIN", "onFocusChange: " + b);
			}
		});


		// setup delegate listener
		newsServiceListener = new NewsServiceListener() {
			@Override
			public void onNewsReceived(ArrayList<News> list, int totalCount, int page) {
				// add to list and show in recyclerview
				newsList.addAll(list);
				totalItems = totalCount;
				currentPage = page;

				handleSearchCompleted();
			}

			@Override
			public void onErrorReceived(CustomError error) {
				//show eror message
				Toast.makeText(getApplicationContext(), error.getErrorMessage(), Toast.LENGTH_SHORT);
				handleSearchCompleted();
			}
		};

		NewsService.getNewsService(this).setServiceListener(newsServiceListener);
	}

	private void handleSearchCompleted() {
		// check network status, show error message if network unavailable
		if (!NetworkService.getNetworkService(this).isNetworkAvailable()) {
			// show network error message
			recycleView.setVisibility(View.GONE);
			tvErrorMessage.setVisibility(View.VISIBLE);

			tvErrorMessage.setText(getString(R.string.text_network_error));
		} else {
			if (totalItems == 0) {
				// hide result view, show error message
				recycleView.setVisibility(View.GONE);
				tvErrorMessage.setVisibility(View.VISIBLE);

				tvErrorMessage.setText(getString(R.string.text_no_result));
			} else {

				if (newsList.size() > 0 && newsList.size() < totalItems) {
					newsAdapter.setFooterEnabled(true);
				} else {
					newsAdapter.setFooterEnabled(false);
				}

				tvErrorMessage.setText("");
				newsAdapter.loadmoreCompleted();

				//notify changes
				newsAdapter.notifyItemInserted(newsList.size());

				recycleView.setVisibility(View.VISIBLE);
				tvErrorMessage.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * Start searching by sending search query to BookService, and update UI when get result
	 * @param keyword: keyword to search
	 */
	private void startSearch(String keyword) {
		keyword = keyword.trim();
		currentKeyword = keyword;

		currentPage += 1;
		// send keyword search to Service
		// build param search
		HashMap params = NewsService.getNewsService(this).baseParams();
		params.put(Constants.URL_PARAM_QUERY, keyword);
		params.put(Constants.URL_PARAM_PAGE, Integer.valueOf(currentPage).toString());

		NewsService.getNewsService(this).startSearch(params);

	}

}
