package datnguyen.com.newsapp.Service;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * Created by datnguyen on 12/26/16.
 */

public class NewsLoader extends AsyncTaskLoader {

	@Override
	public Object loadInBackground() {

		return null;
	}

	public NewsLoader(Context context) {
		super(context);
	}

	@Override
	public void deliverResult(Object data) {
		super.deliverResult(data);
	}

	@Override
	public void deliverCancellation() {
		super.deliverCancellation();
	}

	@Override
	protected void onStopLoading() {
		super.onStopLoading();
	}

	@Override
	protected void onReset() {
		super.onReset();
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
	}
}
