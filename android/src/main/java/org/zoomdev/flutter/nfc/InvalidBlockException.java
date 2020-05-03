package org.zoomdev.flutter.nfc;

public class InvalidBlockException extends NfcException {

    public InvalidBlockException(String message) {
        // super(message);
        super(message, "7");
    }
}
