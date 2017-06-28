package de.jowo.pspac.exceptions;

public class NodeBusyException extends Exception {
	private static final long serialVersionUID = 8150001403468662042L;

	public NodeBusyException() {
		super();
	}

	public NodeBusyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NodeBusyException(String message, Throwable cause) {
		super(message, cause);
	}

	public NodeBusyException(String message) {
		super(message);
	}

	public NodeBusyException(Throwable cause) {
		super(cause);
	}
}
