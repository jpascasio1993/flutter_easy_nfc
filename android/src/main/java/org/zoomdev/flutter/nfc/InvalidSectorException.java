package org.zoomdev.flutter.nfc;

public class InvalidSectorException extends NfcException {

    public InvalidSectorException(String message) {
        // super(message);
        super(message, "8");
    }
}
