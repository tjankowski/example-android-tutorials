package my.todolist.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ToDoItem {
	
	private long id;
	private String text;
	private String title;
	private Date date;
	
	public ToDoItem(String title) {
		this.title = title;
		this.date = new Date();
	}
	
	public ToDoItem(String title, Date date) {
		this.title = title;
		this.date = date;
	}
	
	public ToDoItem(String title, Date date, String text) {
		this.title = title;
		this.date = date;
		this.text = text;
	}
	
	public ToDoItem(long id, String title, Date date, String text) {
		this.id = id;
		this.title = title;
		this.date = date;
		this.text = text;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy");
		return title + " ( " + simpleDateFormat.format(date) + " )";
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

}
