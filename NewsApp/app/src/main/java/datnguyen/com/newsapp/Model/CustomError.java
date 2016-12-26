package datnguyen.com.newsapp.Model;

/**
 * Created by datnguyen on 12/26/16.
 */

public class CustomError {

	public final static int ERROR_EXCEPTION_PARSE = 1;
	public final static int ERROR_SERVER_MESSAGE = 2;
	public final static int ERROR_NETWORK_ERROR = 3;
	public final static int ERROR_RESPONSE_NOT_OK = 4;

	private int errorId;
	private String errorMessage;

	public int getErrorId() {
		return errorId;
	}

	public void setErrorId(int errorId) {
		this.errorId = errorId;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
