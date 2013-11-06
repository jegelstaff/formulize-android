package ca.formulize.android.data;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * This class represents a Formulize Application and its links it contains to
 * different screens/forms
 * 
 * @author timch326
 * 
 */
public class FormulizeApplication implements Parcelable {

	private int appid;
	private String name;
	private String description;
	private FormulizeLink[] links;
	
	public int getAppID() {
		return appid;
	}
	public void setAppID(int appID) {
		this.appid = appID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public FormulizeLink[] getLinks() {
		return links;
	}
	public void setLinks(FormulizeLink[] links) {
		this.links = links;
	}
	
	public String toString() {
		return name;
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
		dest.writeInt(appid);
		dest.writeString(description);
		dest.writeString(name);
		dest.writeParcelableArray(links, 0);
	}

	// static field used to regenerate formulize link data from a parcel
	public static final Parcelable.Creator<FormulizeApplication> CREATOR = new Parcelable.Creator<FormulizeApplication>() {
		public FormulizeApplication createFromParcel(Parcel pc) {
			return new FormulizeApplication(pc);
		}

		@Override
		public FormulizeApplication[] newArray(int size) {
			return new FormulizeApplication[size];
		}
	};

	// Constructor used by CREATOR object to read data from parcel
	public FormulizeApplication(Parcel pc) {
		appid = pc.readInt();
		description = pc.readString();
		name = pc.readString();
		
		Parcelable[] linksParcel = pc.readParcelableArray(FormulizeLink.class.getClassLoader());
		links = new FormulizeLink[linksParcel.length];
		System.arraycopy(linksParcel, 0, links, 0, linksParcel.length);
	
	}
}
