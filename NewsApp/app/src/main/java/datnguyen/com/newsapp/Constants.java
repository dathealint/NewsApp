package datnguyen.com.newsapp;

/**
 * Created by datnguyen on 12/26/16.
 */

public class Constants {

	/* URL API endpoint */
	public final static String URL_BASE = "http://content.guardianapis.com/search";
	public final static String URL_PARAM_APIKEY = "api-key";
	public final static String URL_PARAM_QUERY = "q";
	public final static String URL_PARAM_TAG = "tag";
	public final static String URL_PARAM_FROM_DATE = "from-date";
	public final static String URL_PARAM_ORDER_BY = "order-by";
	public final static String URL_PARAM_PAGE_SIZE = "page-size";
	public final static String URL_PARAM_PAGE = "page";

	public final static String URL_PARAM_SHOW_FIELDS = "show-fields";
	public final static String URL_PARAM_SHOW_FIELDS_VALUES = "thumbnail";


	public final static int REQUEST_ITEMS_PER_PAGE = 10;
	public final static int CONNECTION_TIMEOUT = 30000; //miliseconds

	public final static int DEFAULT_CURRENT_PAGE = 0;



}
