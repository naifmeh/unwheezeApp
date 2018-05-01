package com.unwheeze.unwheezeapp.network;

/**
 * Created by User on 15/03/2018.
 */

public class RequestsScheme {

    public static String HTTP_SCHEME = "http";
    public static String WS_SCHEME = "ws";

    public static String AUTHORITY = "18.219.202.43:8080/Unwheeze";//"192.168.1.96:8080";//
    public static String APP_PATH = "unwheeze";

    public static String WS_REALTIME = "realtime";
    public static String WS_AIRDATA = "airDataFlow";

    public static String AIRDATA_PATH = "airData";
    public static String AIRDATA_GETALL = "getAirCollection";
    public static String AIRDATA_PUTAIRDATA = "putAirData";
    public static String AIRDATA_GETNEAREST = "getNearest";

    public static String AUTH_PATH = "auth";
    public static String AUTH_API_TOKEN = "clientToken";

}