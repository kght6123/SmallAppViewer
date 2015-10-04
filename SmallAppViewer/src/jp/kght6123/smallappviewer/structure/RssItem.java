package jp.kght6123.smallappviewer.structure;

import java.util.Date;

public class RssItem
{
	private static final Date DEFAULT = new Date(0);
	private final CharSequence siteName;
	
	private CharSequence title;
	private CharSequence description;
	private Date date;
	private CharSequence creator;
	private CharSequence link;
	
	public RssItem(final CharSequence siteName)
	{
		super();
		this.siteName = siteName;
	}
	public CharSequence getTitle() {
		return title;
	}
	public CharSequence getDescription() {
		return description;
	}
	public void setTitle(CharSequence title) {
		this.title = title;
	}
	public void setDescription(CharSequence description) {
		this.description = description;
	}
	public Date getDate() {
		if(date != null)
			return date;
		else
			return DEFAULT;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public CharSequence getCreator() {
		return creator;
	}
	public CharSequence getName()
	{
		if(this.creator == null)
			return this.siteName;
		else
			return new StringBuilder().append(this.siteName).append(" by ").append(this.creator).toString();
	}
	public void setCreator(CharSequence creator) {
		this.creator = creator;
	}
	public CharSequence getLink() {
		return link;
	}
	public void setLink(CharSequence link) {
		this.link = link;
	}
}
