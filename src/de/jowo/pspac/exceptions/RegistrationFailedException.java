package de.jowo.pspac.exceptions;

public class RegistrationFailedException extends Exception {

	private static final long serialVersionUID = 7897665149727498638L;

	public RegistrationFailedException() {
		super();
	}

	public RegistrationFailedException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public RegistrationFailedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public RegistrationFailedException(String arg0) {
		super(arg0);
	}

	public RegistrationFailedException(Throwable arg0) {
		super(arg0);
	}

}
