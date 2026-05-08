package com.kinnarastudio.odooxmlrpc.rpc;

import com.kinnarastudio.odooxmlrpc.exception.OdooAuthorizationException;
import org.apache.xmlrpc.XmlRpcException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;

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
     * @param url
     * @param method
     * @param params
     * @return
     * @throws MalformedURLException
     * @throws XmlRpcException
     */
    @Nullable
    @Override
    protected Object execute(String url, String method, Object[] params) throws MalformedURLException, XmlRpcException {
        synchronized (lock) {
            return super.execute(url, method, params);
        }
    }
}
