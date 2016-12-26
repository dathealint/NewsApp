package datnguyen.com.newsapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import java.net.URL;
import java.util.ArrayList;

import datnguyen.com.newsapp.Model.News;
import datnguyen.com.newsapp.Service.LoadmoreInterface;

/**
 * Created by datnguyen on 12/22/16.
 */

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final int VIEW_TYPE_ITEM = 1;
	private final int VIEW_TYPE_PROGRESSBAR = 0;
	private boolean isFooterEnabled = false;

	private boolean isLoadingmore = false;

	private LoadmoreInterface loadmoreInterface;

	private ArrayList<News> newsList;
	public NewsAdapter(ArrayList<News> newsList, LoadmoreInterface loadmoreInterface) {
		this.newsList = newsList;
		this.loadmoreInterface = loadmoreInterface;
	}

	public void setFooterEnabled(boolean footerEnabled) {
		isFooterEnabled = footerEnabled;
	}

	public void loadmoreCompleted() {
		isLoadingmore = false;
	}

	// nested class for ViewHolder
	public static class NewsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		private ImageView imvThumb = null;
		private TextView tvTitle = null;
		private TextView tvSection = null;
		private News news = null;

		public NewsHolder(View itemView) {
			super(itemView);
			this.imvThumb = (ImageView) itemView.findViewById(R.id.imvThumb);
			this.tvSection = (TextView) itemView.findViewById(R.id.tvSection);
			this.tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);

			itemView.setOnClickListener(this);
		}

		public void bindNews(News news) {
			this.news = news;
			this.tvTitle.setText(news.getTitle());
			this.tvSection.setText(news.getSectionName());

			// get imageUrl and use connection to download image
			String imageUrl = news.getThumbnailUrl();
			if (imageUrl != null) {
				Glide.with(MainActivity.getSharedInstance().getApplicationContext()).load(imageUrl).into(this.imvThumb);
			} else {
				// show default image
			}
		}

		@Override
		public void onClick(View view) {
			Log.v("NewsHolder", "DID CLICK News HOLDER");
			// open web
			Intent webIntent = new Intent(Intent.ACTION_VIEW);
			webIntent.setData(Uri.parse(news.getWebUrl()));
			view.getContext().startActivity(webIntent);
		}
	}

	public static class LoadmoreHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private ProgressBar progressBar = null;
		public LoadmoreHolder(View itemView) {
			super(itemView);
			this.progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
		}

		public void bindLoadmore(boolean isLoading) {
			this.progressBar.setIndeterminate(isLoading);
		}

		@Override
		public void onClick(View view) {
			Log.v("NewsHolder", "DID CLICK PROGRESS HOLDER");
		}
	}


	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());

		ViewHolder viewHolder;
		if (viewType == VIEW_TYPE_ITEM) {
			View inflatedView = inflater.inflate(R.layout.news_holder_layout, parent, false);
			viewHolder = new NewsHolder(inflatedView);
		} else {
			View inflatedView = inflater.inflate(R.layout.loadmore_holder_layout, parent, false);
			viewHolder = new LoadmoreHolder(inflatedView);
		}

		return viewHolder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof NewsHolder) {
			News news = newsList.get(position);
			((NewsHolder) holder).bindNews(news);
		} else {
			((LoadmoreHolder) holder).bindLoadmore(true);
			if (!isLoadingmore && loadmoreInterface != null && position == newsList.size()) {
				loadmoreInterface.onLoadmoreBegin();
				isLoadingmore = true;
			}
		}
	}

	@Override
	public int getItemCount() {
		Log.v("Adapter", "getItemCount: "+ newsList.size());

		return (isFooterEnabled) ? newsList.size() + 1 : newsList.size();
	}

	@Override
	public int getItemViewType(int position) {
		if (isFooterEnabled && position >= newsList.size()) {
			return VIEW_TYPE_PROGRESSBAR;
		}
		return VIEW_TYPE_ITEM;
	}
}
