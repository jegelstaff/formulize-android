package ca.formulize.android.connection;

import ca.formulize.android.data.ConnectionInfo;
import ca.formulize.android.data.FormulizeApplication;

public class FUserSession {
	public final static String LOGIN_FAILED = "Login has failed";
	public final static String CONNECTION_FAILED = "Invalid Formulize Connection";

	private static FUserSession instance;
	private ConnectionInfo connectionInfo;
	private String userToken;
	public FormulizeApplication[] applications;

	public static FUserSession getInstance() {
		if (instance == null) {
			instance = new FUserSession();
			return instance;
		} else
			return instance;
	}

	private FUserSession() {
	}

	public ConnectionInfo getConnectionInfo() {
		return this.connectionInfo;
	}

	public void setConnectionInfo(ConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	public String getUserToken() {
		return userToken;
	}
}
