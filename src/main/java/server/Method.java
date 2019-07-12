package server;

public class Method {
    public static String METHOD_GET = "GET";
    public static String METHOD_POST = "POST";
    public static String METHOD_PUT = "PUT";

    public static Boolean validMethod(String method) {
        if (method.equals(METHOD_GET)) return true;
        if (method.equals(METHOD_POST)) return true;
        if (method.equals(METHOD_PUT)) return true;

        return false;
    }
}
