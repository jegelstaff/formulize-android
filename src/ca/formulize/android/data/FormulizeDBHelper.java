package ca.formulize.android.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import ca.formulize.android.data.FormulizeDBContract.ConnectionEntry;

/**
 * A helper class that handles with operations involving Android's SQLite
 * Database.
 * 
 * @author timch326
 * 
 */
public class FormulizeDBHelper extends SQLiteOpenHelper {

	public static final int DATABASE_VERSION = 3;
	public static final String DATABASE_NAME = "Formulize.db";

	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
			+ ConnectionEntry.TABLE_NAME + " (" + ConnectionEntry._ID
			+ " INTEGER PRIMARY KEY,"
			+ ConnectionEntry.COLUMN_NAME_CONNECTION_NAME + " TEXT,"
			+ ConnectionEntry.COLUMN_NAME_CONNECTION_URL + " TEXT,"
			+ ConnectionEntry.COLUMN_NAME_USERNAME + " TEXT,"
			+ ConnectionEntry.COLUMN_NAME_PASSWORD + " TEXT " + " )";

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
			+ ConnectionEntry.TABLE_NAME;

	public FormulizeDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("Formulize", SQL_CREATE_ENTRIES);
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	/**
	 * This inserts ConnectionInfo objects into the SQLite database
	 * 
	 * @param connectionInfo
	 * @return the unique ID of the connection info inserted
	 */
	public long insertConnectionInfo(ConnectionInfo connectionInfo) {
		SQLiteDatabase db = this.getWritableDatabase();

		Log.d("Formulize", "Inserting into Database!");

		ContentValues values = new ContentValues();
		values.put(ConnectionEntry.COLUMN_NAME_CONNECTION_NAME,
				connectionInfo.getConnectionName());
		values.put(ConnectionEntry.COLUMN_NAME_CONNECTION_URL,
				connectionInfo.getConnectionURL());
		values.put(ConnectionEntry.COLUMN_NAME_USERNAME,
				connectionInfo.getUsername());
		values.put(ConnectionEntry.COLUMN_NAME_PASSWORD,
				connectionInfo.getPassword());

		return db.insert(ConnectionEntry.TABLE_NAME, null, values);
	}

	/**
	 * Deletes a single connection from the database
	 * 
	 * @param connectionID
	 *            the id of the connection to be deleted
	 * @return The number of connections deleted
	 */
	public int deleteConnection(long connectionID) {
		SQLiteDatabase db = this.getWritableDatabase();
		String[] args = { Long.toString(connectionID) };
		return db.delete(ConnectionEntry.TABLE_NAME, "_id = ?", args);
	}

	/**
	 * Returns a cursor that selects all the connection info entries saved in
	 * the database. If connection id is specified, it only returns the
	 * connection info with that id.
	 * 
	 * @param connnectionID
	 *            the id of the connection to be selected.
	 * @return a cursor containing all saved connection info if the connectionID
	 *         is less than 0. Otherwise it would only contain the connection
	 *         info with the id specified.
	 */
	public Cursor getConnectionList(long connectionID) {
		String[] projection = { ConnectionEntry._ID,
				ConnectionEntry.COLUMN_NAME_CONNECTION_URL,
				ConnectionEntry.COLUMN_NAME_CONNECTION_NAME,
				ConnectionEntry.COLUMN_NAME_USERNAME,
				ConnectionEntry.COLUMN_NAME_PASSWORD };

		String sortOrder = ConnectionEntry._ID;
		String[] args = { Long.toString(connectionID) };

		SQLiteDatabase db = this.getReadableDatabase();

		if (connectionID < 0) {
			return db.query(ConnectionEntry.TABLE_NAME, projection, null, null,
					null, null, sortOrder);
		} else {
			return db.query(ConnectionEntry.TABLE_NAME, projection, "_id = ?",
					args, null, null, sortOrder);
		}
	}

	/**
	 * Returns a ConnectionInfo with the specified connection id from that
	 * database.
	 * 
	 * @param selectedConnectionID
	 *            the id of the connection info
	 * @return ConnectionInfo with the id specified, it returns null if there is
	 *         no connection with that id
	 */
	public ConnectionInfo getConnection(long selectedConnectionID) {
		Cursor cursor = getConnectionList(selectedConnectionID);

		if (cursor.getCount() > 0) {

			cursor.moveToFirst();

			int connectionURLIndex = cursor
					.getColumnIndex(ConnectionEntry.COLUMN_NAME_CONNECTION_URL);
			int connectionNameIndex = cursor
					.getColumnIndex(ConnectionEntry.COLUMN_NAME_CONNECTION_NAME);
			int usernameIndex = cursor
					.getColumnIndex(ConnectionEntry.COLUMN_NAME_USERNAME);
			int passwordIndex = cursor
					.getColumnIndex(ConnectionEntry.COLUMN_NAME_PASSWORD);

			return new ConnectionInfo(cursor.getString(connectionURLIndex),
					cursor.getString(connectionNameIndex),
					cursor.getString(usernameIndex),
					cursor.getString(passwordIndex));
		} else {
			return null;
		}
	}

	/**
	 * Updates a connection info by replacing the current info with the
	 * connection info specified.
	 * 
	 * @param connectionInfo new Connection Info specified
	 * @param connectionID the connection to be updated
	 * @return indicates update has been successful if it is 1, else it failed
	 */
	public int updateConnectionInfo(ConnectionInfo connectionInfo, long connectionID) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(ConnectionEntry.COLUMN_NAME_CONNECTION_NAME,
				connectionInfo.getConnectionName());
		values.put(ConnectionEntry.COLUMN_NAME_CONNECTION_URL,
				connectionInfo.getConnectionURL());
		values.put(ConnectionEntry.COLUMN_NAME_USERNAME,
				connectionInfo.getUsername());
		values.put(ConnectionEntry.COLUMN_NAME_PASSWORD,
				connectionInfo.getPassword());
		String[] args = { Long.toString(connectionID) };
		
		return db.update(ConnectionEntry.TABLE_NAME, values, "_id = ?", args);
	}

}
