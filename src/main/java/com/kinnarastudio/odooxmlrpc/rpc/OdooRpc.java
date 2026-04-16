package com.kinnarastudio.odooxmlrpc.rpc;

import com.kinnarastudio.commons.Try;
import com.kinnarastudio.odooxmlrpc.annotation.OdooField;
import com.kinnarastudio.odooxmlrpc.annotation.OdooModel;
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
     * <p>
     * Retrieve fields on the model
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
     * Fields Get
     * <p>
     * Retrieve fields on the model
     *
     * @param tClass
     * @return
     * @throws OdooCallMethodException
     */
    public Collection<Field> fieldsGet(Class<?> tClass) throws OdooCallMethodException {
        String model = getModel(tClass);
        return fieldsGet(model);
    }

    /**
     * Search
     * <p>
     * Implementation of odoo's xmlrpc <b>search()</b> method
     *
     * @param tClass
     * @param filters
     * @param order
     * @param offset
     * @param limit
     * @param <T>
     * @return
     * @throws OdooCallMethodException
     */
    public <T> Integer[] search(Class<T> tClass, SearchFilter[] filters, String order, Integer offset, Integer limit) throws OdooCallMethodException {
        String model = getModel(tClass);
        return search(model, filters, order, offset, limit);
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

            final Object[] objectFilters = XmlRpcUtil.prefixation(filters);

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

            final Object[] objectFilters = XmlRpcUtil.prefixation(filters);

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
     * Search Read
     * <p>
     * Implementation of odoo's xmlrpc <b>search_read()</b> method
     *
     * @param tClass
     * @param filters
     * @param order
     * @param offset
     * @param limit
     * @param <T>
     * @return
     * @throws OdooCallMethodException
     */
    public <T> T[] searchRead(Class<T> tClass, SearchFilter[] filters, String order, Integer offset, Integer limit) throws OdooCallMethodException {
        String model = getModel(tClass);
        Map<String, Object>[] records = searchRead(model, filters, order, offset, limit);

        return Arrays.stream(records)
                .map(m -> parseRecord(tClass, m))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray(size -> (T[]) java.lang.reflect.Array.newInstance(tClass, size));
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

            final Object[] objectFilters = XmlRpcUtil.prefixation(filters);

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

    public int searchCount(Class<?> tClazz, SearchFilter[] filters) throws OdooCallMethodException {
        String model = getModel(tClazz);
        return searchCount(model, filters);
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
     *
     * @param tClass
     * @param recordId
     * @return
     * @throws OdooCallMethodException
     */
    public Optional<Map<String, Object>> read(Class<?> tClass, int recordId) throws OdooCallMethodException {
        String model = getModel(tClass);
        return read(model, recordId);
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
    public int create(String model, Map<String, Object> record) throws OdooCallMethodException {
        try {
            final int uid = login();

            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "create",
                    new Object[]{record}
            };

            return (int) XmlRpcUtil.execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);

        } catch (MalformedURLException | XmlRpcException | OdooAuthorizationException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     *
     * @param record
     * @return
     * @throws OdooCallMethodException
     */
    public <T> int create(T record) throws OdooCallMethodException {
        String model = getModel(record.getClass());
        Map<String, Object> map = getRowMap(record);
        return create(model, map);
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
    public void write(String model, int recordId, Map<String, Object> record) throws OdooCallMethodException {
        try {
            final int uid = login();

            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "write",
                    new Object[]{recordId, record},
            };

            XmlRpcUtil.execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);

        } catch (MalformedURLException | XmlRpcException | OdooAuthorizationException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     *
     * @param recordId
     * @param record
     * @throws OdooCallMethodException
     */
    public <T> void write(int recordId, T record) throws OdooCallMethodException {
        String model = getModel(record.getClass());
        Map<String, Object> map = getRowMap(record);
        write(model, recordId, map);
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

    public void unlink(Class<?> tClass, int recordId) throws OdooCallMethodException {
        String model = getModel(tClass);
        unlink(model, recordId);
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

    /**
     *
     * @param tClass
     * @param recordId
     * @param body
     * @return
     * @throws OdooCallMethodException
     */
    public int messagePost(Class<?> tClass, int recordId, String body) throws OdooCallMethodException {
        String model = getModel(tClass);
        return messagePost(model, new int[]{recordId}, MessageType.COMMENT, body);
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

    protected String getModel(Class<?> tClass) throws OdooCallMethodException {
        return Optional.of(tClass)
                .map(c -> c.getAnnotation(OdooModel.class))
                .map(OdooModel::value)
                .orElseThrow(() -> new OdooCallMethodException("Class [" + tClass.getName() + "] is not annotated with @OdooModel"));
    }

    protected <T> Map<String, Object> getRowMap(T record) {

        Map<String, Object> map = new HashMap<>();

        Optional.ofNullable(record)
                .map(Object::getClass)
                .map(Class::getDeclaredFields)
                .stream()
                .flatMap(Arrays::stream)
                .forEach(Try.onConsumer(f -> {
                    f.setAccessible(true);
                    String key = Optional.of(OdooField.class)
                            .map(f::getAnnotation)
                            .map(OdooField::value)
                            .orElse(f.getName());
                    Object value = f.get(record);
                    map.put(key, value);
                }, (Exception e) -> {
                }));

        return map;
    }

    protected <T> Optional<T> parseRecord(Class<T> tClass, Map<String, Object> record) {
        try {
            T instance = tClass.getDeclaredConstructor().newInstance();

            Optional.of(tClass)
                    .map(Class::getDeclaredFields)
                    .stream()
                    .flatMap(Arrays::stream)
                    .forEach(Try.onConsumer(field -> {
                        field.setAccessible(true);

                        // get fieldname from either annotation or field declaration
                        String fieldName = Optional.of(OdooField.class)
                                .map(field::getAnnotation)
                                .map(OdooField::value)
                                .orElseGet(field::getName);

                        if(record.containsValue(fieldName)) {
                            Object value = record.get(fieldName);
                            field.set(instance, value);
                        }
                    }));

            return Optional.of(instance);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
