package org.ox17.kcimagecollector;

import java.util.LinkedList;
import java.util.List;

public class IterationState {
	public IterationState(String boardName) {
		super();
		this.boardName = boardName;
		this.visitedThreadLinks = new LinkedList<String>();
		this.pageLinks = new LinkedList<String>();
	}
	public String boardName;
	public List<String> visitedThreadLinks;
	public List<String> pageLinks;
	public boolean done;
}