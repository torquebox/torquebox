package org.torquebox.integration;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;

public class FancyHtmlUnitDriver extends HtmlUnitDriver implements CredentialsProvider {

    DefaultCredentialsProvider credentialsProvider;

    public FancyHtmlUnitDriver() {
        this.credentialsProvider = new DefaultCredentialsProvider();
    }

    @Override
    protected WebClient modifyWebClient(WebClient client) {
        client.setCredentialsProvider( this );
        return client;
    }

    public void setCredentials(String username, String password) {
        resetCredentials();
        this.credentialsProvider.addCredentials( username, password );
    }

    public void resetCredentials() {
        this.credentialsProvider = new DefaultCredentialsProvider();
    }

    @Override
    public Credentials getCredentials(AuthScheme scheme, String host, int port, boolean proxy) throws CredentialsNotAvailableException {
        return this.credentialsProvider.getCredentials( scheme, host, port, proxy );
    }

}
