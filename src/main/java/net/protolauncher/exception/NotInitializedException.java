package net.protolauncher.exception;

/**
 * An exception representing a situation where a method was called before
 * the instance variables it requires have been loaded or initialized.
 */
public class NotInitializedException extends RuntimeException {

    public NotInitializedException() {
        super("One or more required instance variables for this method has not been initialized yet!");
    }

}
