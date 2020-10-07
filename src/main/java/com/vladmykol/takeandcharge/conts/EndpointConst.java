package com.vladmykol.takeandcharge.conts;

public class EndpointConst {
    public static final String API_AUTH = "/auth";
    public static final String API_AUTH_LOGIN = "/login";
    public static final String API_AUTH_REGISTER_INIT = "/register";
    public static final String API_AUTH_REGISTER = "/singup";
    public static final String API_AUTH_LOGOUT = "/logout";
    public static final String API_RENT = "/rent";
    public static final String API_STATIONS = "/stations";
    public static final String API_ID = "/{id}";
    public static final String API_STATIONS_CAPACITY = API_ID + "/capacity";
    public static final String API_STATIONS_NEARBY = "/nearby";
    public static final String API_PAY = "/pay";
    public static final String API_APP = "/app";
    public static final String API_PAY_CHECKOUT = "/checkout";
    public static final String API_PAY_CALLBACK = "/callback";
    public static final String API_PAY_CALLBACK_AUTH = API_PAY_CALLBACK + "/auth";
    public static final String API_PAY_CALLBACK_HOLD = API_PAY_CALLBACK + "/hold";
    public static final String API_SOCKET_RENT = "/socket/rent";
    public static final String API_SMS = "/sms";
    public static final String API_SMS_CALLBACK = "/callback";
    public static final String API_USER = "/user";
    public static final String API_ADMIN_ONLINE_CLIENT = "/online-client";
    public static final String API_ADMIN = "/admin";
}
