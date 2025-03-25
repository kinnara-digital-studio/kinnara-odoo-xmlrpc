package com.kinnarastudio.odooxmlrpc.rpc;

import com.kinnarastudio.commons.Try;
import com.kinnarastudio.odooxmlrpc.XmlRpcUtil;
import com.kinnarastudio.odooxmlrpc.exception.OdooAuthorizationException;
import com.kinnarastudio.odooxmlrpc.exception.OdooCallMethodException;
import org.apache.xmlrpc.XmlRpcException;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Odoo RPC
 *
 * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#external-api">External API</a>
 */
public class OdooRpc {
    public final static String PATH_COMMON = "/xmlrpc/2/common";
    public final static String PATH_OBJECT = "/xmlrpc/2/object";
    private final String baseUrl;
    private final String database;
    private final String user;
    private final String apiKey;

    private final static Logger logger = Logger.getLogger(OdooRpc.class.getName());

    public OdooRpc(String baseUrl, String database, String user, String apiKey) {
        this.baseUrl = baseUrl;
        this.database = database;
        this.user = user;
        this.apiKey = apiKey;
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
    public int login() throws OdooAuthorizationException {
        try {
            final Object ret = XmlRpcUtil.execute(baseUrl + "/" + PATH_COMMON, "login", new Object[]{database, user, apiKey});

            if (ret instanceof Integer) {
                return (int) ret;
            } else {
                throw new OdooAuthorizationException("Invalid login authorization for user [" + user + "] database [" + database + "] apiKey [" + apiKey + "]");
            }

        } catch (XmlRpcException | MalformedURLException e) {
            throw new OdooAuthorizationException(e);
        }
    }

    /**
     * Fields Get
     * <p>
     * Implementation of odoo's xmlrpc <b>fields_get()</b> method
     *
     * @param model
     * @return
     * @throws OdooCallMethodException
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#list-record-fields">List record fields</a>
     */
    @Nonnull
    public Map<String, Map<String, Object>> fieldsGet(String model) throws OdooCallMethodException {
        try {
            final int uid = login();

            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "fields_get",
                    Collections.emptyMap()
            };

            final Object ret = XmlRpcUtil.execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);
            return Optional.ofNullable((Map<String, Map<String, Object>>) ret)
                    .orElseGet(Collections::emptyMap);

        } catch (MalformedURLException | XmlRpcException | OdooAuthorizationException e) {
            throw new OdooCallMethodException(e);
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
    @Nonnull
    public Integer[] search(String model, SearchFilter[] filters, String order, Integer offset, Integer limit) throws OdooCallMethodException {
        try {
            final int uid = login();

            final Object[] objectFilters = Optional.ofNullable(filters)
                    .stream()
                    .flatMap(Arrays::stream)
                    .map(f -> new Object[]{f.getField(), f.getOperator(), f.getValue()})
                    .toArray(Object[]::new);

            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "search",
                    new Object[]{objectFilters},
                    new HashMap<String, Object>() {{
                        if (offset != null) put("offset", offset);
                        if (limit != null) put("limit", limit);
                        if (order != null) put("order", order);
                    }}
            };

            return Arrays.stream((Object[]) XmlRpcUtil.execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params))
                    .map(o -> (Integer) o)
                    .toArray(Integer[]::new);

        } catch (MalformedURLException | XmlRpcException | OdooAuthorizationException e) {
            throw new OdooCallMethodException(e);
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
    @Nonnull
    public Map<String, Object>[] searchRead(String model, SearchFilter[] filters, String order, Integer offset, Integer limit) throws OdooCallMethodException {
        try {
            final int uid = login();

            final Object[] objectFilters = Optional.ofNullable(filters)
                    .stream()
                    .flatMap(Arrays::stream)
                    .map(f -> new Object[]{f.getField(), f.getOperator(), f.getValue()})
                    .toArray(Object[]::new);


            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "search_read",
                    new Object[]{objectFilters},
                    new HashMap<String, Object>() {{
                        if (offset != null) put("offset", offset);
                        if (limit != null) put("limit", limit);
                        if (order != null) put("order", order);
                    }}
            };

            return Arrays.stream((Object[]) XmlRpcUtil.execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params))
                    .map(o -> (Map<String, Object>) o)
                    .peek(m -> m.forEach((key, value) -> {
                        if (value instanceof Boolean && !(boolean) value) m.replace(key, null);
                    }))
                    .toArray(Map[]::new);

        } catch (MalformedURLException | XmlRpcException | OdooAuthorizationException e) {
            throw new OdooCallMethodException(e);
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
    public int searchCount(String model, SearchFilter[] filters) throws OdooCallMethodException {
        try {
            final int uid = login();

            final Object[] objectFilters = Optional.ofNullable(filters)
                    .map(Arrays::stream)
                    .orElseGet(Stream::empty)
                    .map(f -> new Object[]{f.getField(), f.getOperator(), f.getValue()})
                    .toArray(Object[]::new);

            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "search_count",
                    new Object[]{objectFilters}
            };

            return Optional.ofNullable((int) XmlRpcUtil.execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params))
                    .orElse(0);

        } catch (MalformedURLException | XmlRpcException | OdooAuthorizationException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     * Read
     * <p>
     * Implementation of odoo's xmlrpc <b>read()</b>
     *
     * @param model
     * @param recordId
     * @return
     * @throws OdooCallMethodException
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#read-records">Read records</a>
     */
    public Optional<Map<String, Object>> read(String model, int recordId) throws OdooCallMethodException {
        try {
            final int uid = login();

            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "read",
                    new Object[]{recordId},
            };

            return Arrays.stream((Object[]) XmlRpcUtil.execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params))
                    .findFirst()
                    .map(o -> (Map<String, Object>) o)
                    .map(Try.toPeek(m -> m.forEach((key, value) -> {
                        if (value instanceof Boolean && !(boolean) value) m.replace(key, null);
                    })));

        } catch (MalformedURLException | XmlRpcException | OdooAuthorizationException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     * Create
     * <p>
     * Implementation of odoo's xmlrpc <b>create()</b>
     *
     * @param model
     * @param row
     * @return
     * @throws OdooCallMethodException
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#create-records">Create records</a>
     */
    public int create(String model, Map<String, Object> row) throws OdooCallMethodException {
        try {
            final int uid = login();

            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "create",
                    new Object[]{row},
            };

            int recordId = (int) XmlRpcUtil.execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);

            logger.info("rpc create : model [" + model + "] new record has been created with id [" + recordId + "]");

            return recordId;

        } catch (MalformedURLException | XmlRpcException | OdooAuthorizationException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     * Write
     * <p>
     * Implementation of odoo's xmlrpc <b>write()</b>
     *
     * @param model
     * @param recordId
     * @param row
     * @throws OdooCallMethodException
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#update-records">Update records</a>
     */
    public void write(String model, int recordId, Map<String, Object> row) throws OdooCallMethodException {
        try {
            final int uid = login();

            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "write",
                    new Object[]{recordId, row},
            };

            XmlRpcUtil.execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);

            logger.info("rpc write : model [" + model + "] record id [" + recordId + "] has been update");

        } catch (MalformedURLException | XmlRpcException | OdooAuthorizationException e) {
            throw new OdooCallMethodException(e);
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
    public void unlink(String model, int recordId) throws OdooCallMethodException {
        try {
            final int uid = login();

            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "unlink",
                    new Object[]{recordId},
            };

            XmlRpcUtil.execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);
            logger.info("rpc unlink : model [" + model + "] record [" + recordId + "] has been deleted");

        } catch (MalformedURLException | XmlRpcException | OdooAuthorizationException e) {
            throw new OdooCallMethodException(e);
        }
    }

    public void unlink(String model, int[] recordIds) throws OdooCallMethodException {
        try {
            final int uid = login();

            final Object[] objectFilters = Arrays.stream(recordIds)
                    .mapToObj(i -> new Object[]{"id", "=", i})
                    .toArray(Object[]::new);

            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "unlink",
                    new Object[]{objectFilters},
            };

            XmlRpcUtil.execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);
            logger.info("rpc unlink : model [" + model + "] record [" + Arrays.stream(recordIds).mapToObj(String::valueOf).collect(Collectors.joining(",")) + "] have been deleted");

        } catch (MalformedURLException | XmlRpcException | OdooAuthorizationException e) {
            throw new OdooCallMethodException(e);
        }
    }
}
