package az.inci.wms;

public class Utilities {
    public static String getMessage(Exception e) {
        String message;
        Throwable throwable = e;
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        message = throwable.toString();

        return message;
    }
    public static String getClearMessage(Exception e) {
        String message;
        Throwable throwable = e;
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        message = throwable.getMessage();

        return message;
    }
}
