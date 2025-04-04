package com.example.nirvana.api;

import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1RequestToken;

public class FatSecretOAuthApi extends DefaultApi10a {
    private static final FatSecretOAuthApi INSTANCE = new FatSecretOAuthApi();

    public static FatSecretOAuthApi instance() {
        return INSTANCE;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return "https://www.fatsecret.com/oauth/request_token";
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://www.fatsecret.com/oauth/access_token";
    }

    @Override
    public String getAuthorizationBaseUrl() {
        return "https://www.fatsecret.com/oauth/authorize";
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return getAuthorizationBaseUrl() + "?oauth_token=" + requestToken.getToken();
    }
}