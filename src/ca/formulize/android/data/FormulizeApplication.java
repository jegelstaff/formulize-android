package ca.formulize.android.data;

import java.util.List;

/**
 * This class represents a Formulize Application and its links it contains to
 * different screens/forms
 * 
 * @author timch326
 * 
 */
public class FormulizeApplication {

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
	
}
