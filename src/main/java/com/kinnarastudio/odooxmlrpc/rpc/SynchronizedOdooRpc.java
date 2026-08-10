package com.kinnarastudio.odooxmlrpc.rpc;

import com.kinnarastudio.odooxmlrpc.exception.OdooAuthorizationException;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClientConfig;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Odoo RPC
 *
 * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#external-api">External API</a>
 */
public class SynchronizedOdooRpc extends OdooRpc {
    private static final Object lock = new Object();

    public SynchronizedOdooRpc(@Nonnull String baseUrl, @Nonnull String database, @Nonnull String user, @Nonnull String apiKey) throws OdooAuthorizationException {
        super(baseUrl, database, user, apiKey);
    }

    /**
     *
     * @param url    The url
     * @param method The method
     * @param params The parameters
     * @return
     * @throws XmlRpcException
     * @throws MalformedURLException
     */
    @Nullable
    @Override
    protected Object execute(String url, String method, Object[] params) throws XmlRpcException, MalformedURLException {
        synchronized (lock) {
            return super.execute(url, method, params);
        }
    }
}
