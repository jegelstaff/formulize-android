package ca.formulize.android.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This represents a single menu link to a Formulize screen/form within a
 * Formulize Application
 * 
 * @author timch326
 * 
 */
public class FormulizeLink implements Parcelable {

	private int menuID;
	private int appid;
	private int rank;
	private String screen;
	private String url;
	private String linkText;
	private String name;
	private String text;

	public int getMenuID() {
		return menuID;
	}

	public void setMenuID(int menuID) {
		this.menuID = menuID;
	}

	public int getAppid() {
		return appid;
	}

	public void setAppid(int appid) {
		this.appid = appid;
	}

	public String getScreen() {
		return screen;
	}

	public void setScreen(String screen) {
		this.screen = screen;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLinkText() {
		return linkText;
	}

	public void setLinkText(String linkText) {
		this.linkText = linkText;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String toString() {
		return text;
	}

	/*
	 * Implementation of the Parcelable interface below
	 * @see android.os.Parcelable
	 */
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeIntArray(new int[] { this.menuID, this.appid, this.rank });
		dest.writeStringArray(new String[] { this.screen, this.url,
				this.linkText, this.name, this.text });

	}

	// static field used to regenerate formulize link data from a parcel
	public static final Parcelable.Creator<FormulizeLink> CREATOR = new Parcelable.Creator<FormulizeLink>() {
		public FormulizeLink createFromParcel(Parcel pc) {
			return new FormulizeLink(pc);
		}

		@Override
		public FormulizeLink[] newArray(int size) {
			return new FormulizeLink[size];
		}
	};

	// Constructor used by CREATOR object to read data from parcel
	public FormulizeLink(Parcel pc) {
		int[] intData = new int[3];
		pc.readIntArray(intData);
		String[] stringData = new String[5];
		pc.readStringArray(stringData);
		
		menuID = intData[0];
		appid = intData[1];
		rank = intData[2];
		
		screen = stringData[0];
		url = stringData[1];
		linkText = stringData[2];
		name = stringData[3];
		text = stringData[4];
	}

}
