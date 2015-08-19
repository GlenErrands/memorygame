package io.github.glenerrands.memorygame;

/**
 * Generic exception to demonstrate multi-catch.
 */
public class MemoryGameException extends Exception {

	private static final long serialVersionUID = 298429286366783948L;

	public MemoryGameException() {
	}

	public MemoryGameException(String message) {
		super(message);
	}

	public MemoryGameException(Throwable cause) {
		super(cause);
	}

	public MemoryGameException(String message, Throwable cause) {
		super(message, cause);
	}

	public MemoryGameException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
