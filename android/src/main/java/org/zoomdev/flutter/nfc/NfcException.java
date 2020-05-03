package org.zoomdev.flutter.nfc;

public class NfcException extends Exception {
    private String message;
    private String errorCode;

    public NfcException(String message, String errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }


    @Override
    public String toString() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    //    private short sw;
//
//    public NfcException(short sw) {
//        this.sw = sw;
//    }
//
//    public String getMessage() {
//        return String.format("%02x%02x", this.sw & 255, this.sw >> 8 & 255);
//    }
//
//    public boolean is(String sw) {
//        int nSw = Integer.parseInt(sw, 16) & '\uffff';
//        return (nSw & 255) == (this.sw >> 8 & 255) && (nSw >> 8 & 255) == (this.sw & 255);
//    }
}
