package com.kinnarastudio.odooxmlrpc.rpc;

import com.kinnarastudio.odooxmlrpc.exception.OdooAuthorizationException;
import com.kinnarastudio.odooxmlrpc.exception.OdooCallMethodException;
import com.kinnarastudio.odooxmlrpc.model.Field;
import com.kinnarastudio.odooxmlrpc.model.MessageType;
import com.kinnarastudio.odooxmlrpc.model.SearchFilter;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * Odoo RPC
 *
 * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#external-api">External API</a>
 */
public class SynchronizedOdooRpc extends OdooRpc {
    private static final Object lock = new Object();

    public SynchronizedOdooRpc(String baseUrl, String database, String user, String apiKey) throws OdooAuthorizationException {
        super(baseUrl, database, user, apiKey);
    }

    /**
     * Login
     * <p>
     * Authenticate
     *
     * @return
     * @throws OdooAuthorizationException
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#logging-in">Loggin in</a>
     */
    @Override
    public int login() throws OdooAuthorizationException {
        synchronized (lock) {
            return super.login();
        }
    }

    /**
     * Fields Get
     * <p>
     * Implementation of odoo's xmlrpc <b>fields_get()</b> method
     * <p>
     * Retrieve fields on the model
     *
     * @param model
     * @return
     * @throws OdooCallMethodException
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#list-record-fields">List record fields</a>
     */
    @Override
    @Nonnull
    public Collection<Field> fieldsGet(String model) throws OdooCallMethodException {
        synchronized (lock) {
            return super.fieldsGet(model);
        }
    }

    /**
     * Search
     * <p>
     * Implementation of odoo's xmlrpc <b>search()</b> method
     *
     * @param model
     * @param filters
     * @param order
     * @param offset
     * @param limit
     * @return
     * @throws OdooCallMethodException
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#list-records">List Records</a>
     */
    @Override
    @Nonnull
    public Integer[] search(String model, SearchFilter[] filters, String order, Integer offset, Integer limit) throws OdooCallMethodException {
        synchronized (lock) {
            return super.search(model, filters, order, offset, limit);
        }
    }

    /**
     * Search Read
     * <p>
     * Implementation of odoo's xmlrpc <b>search_read()</b> method
     *
     * @param model
     * @param filters
     * @param order
     * @param offset
     * @param limit
     * @return
     * @throws OdooCallMethodException
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#search-and-read">Search and Read</a>
     */
    @Override
    @Nonnull
    public Map<String, Object>[] searchRead(String model, String[] fields, SearchFilter[] filters, String order, Integer offset, Integer limit) throws OdooCallMethodException {
        synchronized (lock) {
            return super.searchRead(model, fields, filters, order, offset, limit);
        }
    }

    /**
     * Search Count
     * <p>
     * Implementation of odoo's xmlrpc <b>search_count()</b>
     *
     * @param model
     * @param filters
     * @return
     * @throws OdooCallMethodException
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#count-records">Count records</a>
     */
    @Override
    public int searchCount(String model, SearchFilter[] filters) throws OdooCallMethodException {
        synchronized (lock) {
            return super.searchCount(model, filters);
        }
    }

    /**
     * Create
     * <p>
     * Implementation of odoo's xmlrpc <b>create()</b>
     *
     * @param model
     * @param record
     * @return
     * @throws OdooCallMethodException
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#create-records">Create records</a>
     */
    @Override
    public int create(String model, Map<String, Object> record) throws OdooCallMethodException {
        synchronized (lock) {
            return super.create(model, record);
        }
    }

    /**
     * Write
     * <p>
     * Implementation of odoo's xmlrpc <b>write()</b>
     *
     * @param model
     * @param recordId
     * @param record
     * @throws OdooCallMethodException
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#update-records">Update records</a>
     */
    @Override
    public void write(String model, int recordId, Map<String, Object> record) throws OdooCallMethodException {
        synchronized (lock) {
            super.write(model, recordId, record);
        }
    }

    /**
     * Unlink
     * <p>
     * Implementation of odoo's xmlrpc <b>unlink()</b>
     *
     * @param model
     * @param recordId
     * @throws OdooCallMethodException
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#delete-records">Delete records</a>
     */
    @Override
    public void unlink(String model, int recordId) throws OdooCallMethodException {
        synchronized (lock) {
            super.unlink(model, recordId);
        }
    }

    /**
     *
     * @param model
     * @param recordIds
     * @param messageType
     * @param body
     * @return
     * @throws OdooCallMethodException
     */
    @Override
    public int messagePost(String model, int[] recordIds, MessageType messageType, String body) throws OdooCallMethodException {
        synchronized (lock) {
            return super.messagePost(model, recordIds, messageType, body);
        }
    }
}
