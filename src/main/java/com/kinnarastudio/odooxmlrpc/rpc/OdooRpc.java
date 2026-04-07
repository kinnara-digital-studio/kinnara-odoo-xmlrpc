package com.kinnarastudio.odooxmlrpc.rpc;

import com.kinnarastudio.commons.Try;
import com.kinnarastudio.odooxmlrpc.exception.OdooAuthorizationException;
import com.kinnarastudio.odooxmlrpc.exception.OdooCallMethodException;
import com.kinnarastudio.odooxmlrpc.model.Field;
import com.kinnarastudio.odooxmlrpc.model.MessageType;
import com.kinnarastudio.odooxmlrpc.model.SearchFilter;
import org.apache.xmlrpc.XmlRpcException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

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
    public Collection<Field> fieldsGet(String model) throws OdooCallMethodException {
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
                    .map(Map::entrySet)
                    .stream()
                    .flatMap(Collection::stream)
                    .map(e -> new Field(e.getKey(), e.getValue()))
                    .collect(Collectors.toSet());

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

            final Object[] objectFilters = prefixization(filters);

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

            final Object[] objectFilters = prefixization(filters);

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

            final Object[] objectFilters = prefixization(filters);

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

            return (int) XmlRpcUtil.execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);

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

        } catch (MalformedURLException | XmlRpcException | OdooAuthorizationException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     *
     * @param model
     * @param recordId
     * @param body
     */
    public int messagePost(String model, int recordId, String body) throws OdooCallMethodException {
        return messagePost(model, new int[]{recordId}, MessageType.COMMENT, body);
    }

    public int messagePost(String model, int[] recordIds, MessageType messageType, String body) throws OdooCallMethodException {
        try {
            final int uid = login();
            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "message_post",
                    recordIds,
                    new HashMap<String, Object>() {{
                        put("body", body);
                        put("message_type", messageType.name().toLowerCase());
                        put("subtype_xmlid", "mail.mt_comment");
                    }}
            };

            return (int) XmlRpcUtil.execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);
        } catch (OdooAuthorizationException | MalformedURLException | XmlRpcException e) {
            throw new OdooCallMethodException(e);
        }
    }

    public Object[] prefixization(SearchFilter[] filters) {
        if (filters == null || filters.length == 0) {
            return new Object[0];
        }

        List<Object> result = new ArrayList<>();

        for (SearchFilter filter : filters) {
            if (result.isEmpty()) {
                result.add(new SearchFilter.Operand(filter));
            } else {
                SearchFilter.Join operator = filter.getJoin();
                result.add(0, operator);
                result.add(new SearchFilter.Operand(filter));
            }
        }

        return result.stream()
                .map(o -> {
                    if (o instanceof SearchFilter.Operand) {
                        return ((SearchFilter.Operand) o).toObjects();
                    } else if (o instanceof SearchFilter.Join) {
                        return o.toString();
                    } else {
                        return o;
                    }
                }).toArray();
    }


    @Nullable
    public Object[] mathematicPrefixization(SearchFilter[] filters) {
        if (filters == null || filters.length == 0) {
            return new Object[0];
        }

        Stack<List<Object>> operandStack = new Stack<>();
        Stack<SearchFilter.Join> operatorStack = new Stack<>();

        for (SearchFilter filter : filters) {
            if (operandStack.isEmpty()) {
                operandStack.push(new ArrayList<>() {{
                    add(new SearchFilter.Operand(filter));
                }});
            } else {
                SearchFilter.Join operator = filter.getJoin();
                if (operator == SearchFilter.Join.OR) {
                    operatorStack.push(operator);
                    operandStack.push(new ArrayList<>() {{
                        add(new SearchFilter.Operand(filter));
                    }});
                } else {
                    List<Object> pop = operandStack.pop();
                    pop.add(0, operator);
                    pop.add(0, new SearchFilter.Operand(filter));
                    operandStack.push(pop);
                }
            }
        }

        List<Object> result = new ArrayList<>();
        while (!operatorStack.isEmpty()) {
            SearchFilter.Join operator = operatorStack.pop();
            List<Object> right = operandStack.pop();
            List<Object> left = operandStack.pop();

            result.add(operator);
            result.add(left);
            result.add(right);
        }

        return result.toArray();
    }


}
