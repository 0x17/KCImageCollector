package org.ox17.kcimagecollector;

public class InvalidBoardNameException extends Exception {
	private final String boardName;

	public InvalidBoardNameException(String boardName) {
		super();
		this.boardName = boardName;
	}
	@Override
	public String getMessage() {
		return "Invalid board name: "+boardName
				+". Always use board names like \"/c/\" where c is at least one lowercase character!";
	}
}
