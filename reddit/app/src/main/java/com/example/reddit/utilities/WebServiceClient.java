package com.example.reddit.utilities;


import com.loopj.android.http.*;

/**
 * Created by Maxim on 12/6/2015.
 * Wrapper sur la librairie loopj afin de faire des requêtes web (post/get)
 */
public class WebServiceClient {

    private static final String BASE_URL = "https://www.reddit.com/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    /**
     *
     * Fait un get sur l'url
     *
     * @param url l'url relative sur laquelle on fait une requêtes (sans l'url de base) ex: /getthis à la place de www.google.com/gethis
     * @param params Les paramètres de la requête web
     * @param responseHandler Le handler pour la réponse
     */
    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    /**
     *
     * Fait un post sur l'url
     *
     * @param url l'url sur laquelle on fait une requêtes (sans l'url de base) ex: /getthis à la place de www.google.com/gethis
     * @param params Les paramètres de la requête web
     * @param responseHandler Le handler pour la réponse
     */
    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    /**
     *
     * Retourne l'url absolue (base url + relative)
     *
     * @param relativeUrl
     * @return
     */
    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

}
