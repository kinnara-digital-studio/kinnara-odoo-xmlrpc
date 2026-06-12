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
    private final int uid;

    /**
     * OdooRpc constructor
     * @param baseUrl The odoo base url
     * @param database The database name
     * @param user The username
     * @param apiKey The api key
     * @throws OdooAuthorizationException when authorization failed
     */
    public OdooRpc(@Nonnull String baseUrl, @Nonnull String database, @Nonnull String user, @Nonnull String apiKey) throws OdooAuthorizationException {
        this.baseUrl = baseUrl;
        this.database = database;
        this.user = user;
        this.apiKey = apiKey;
        this.uid = login();
    }

    /**
     * Login
     * <p>
     * Authenticate
     *
     * @return uid
     * @throws OdooAuthorizationException when authorization failed
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#logging-in">Loggin in</a>
     */
    public int login() throws OdooAuthorizationException {
        try {
            final Object ret = execute(baseUrl + "/" + PATH_COMMON, "login", new Object[]{database, user, apiKey});

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
     * @param model The odoo model
     * @return a collection of Field
     * @throws OdooCallMethodException when calling method failed
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#list-record-fields">List record fields</a>
     */
    @Nonnull
    public Collection<Field> fieldsGet(@Nonnull String model) throws OdooCallMethodException {
        try {
            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "fields_get",
                    Collections.emptyMap()
            };

            final Object ret = execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);

            return Optional.ofNullable((Map<String, Map<String, Object>>) ret)
                    .map(Map::entrySet)
                    .stream()
                    .flatMap(Collection::stream)
                    .map(e -> new Field(e.getKey(), e.getValue()))
                    .collect(Collectors.toSet());

        } catch (MalformedURLException | XmlRpcException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     * Fields Get
     * <p>
     * Retrieve fields on the model
     *
     * @param tClass The class that is annotated with {@link OdooModel}
     * @return a collection of Field
     * @throws OdooCallMethodException when calling method failed
     */
    public Collection<Field> fieldsGet(@Nonnull Class<?> tClass) throws OdooCallMethodException {
        String model = getModel(tClass);
        return fieldsGet(model);
    }

    /**
     * Search
     * <p>
     * Implementation of odoo's xmlrpc <b>search()</b> method
     *
     * @param tClass The class that is annotated with {@link OdooModel}
     * @param filters An array of {@link SearchFilter}
     * @param order The order
     * @param offset The offset
     * @param limit The limit
     * @param <T> The type of class
     * @return an array of record id
     * @throws OdooCallMethodException when calling method failed
     */
    public <T> Integer[] search(@Nonnull Class<T> tClass, SearchFilter[] filters, String order, Integer offset, Integer limit) throws OdooCallMethodException {
        String model = getModel(tClass);
        return search(model, filters, order, offset, limit);
    }


    /**
     * Search
     * <p>
     * Implementation of odoo's xmlrpc <b>search()</b> method
     *
     * @param model The odoo model
     * @param filters An array of {@link SearchFilter}
     * @param order The order
     * @param offset The offset
     * @param limit The limit
     * @return an array of record id
     * @throws OdooCallMethodException when calling method failed
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#list-records">List Records</a>
     */
    @Nonnull
    public Integer[] search(@Nonnull String model, SearchFilter[] filters, String order, Integer offset, Integer limit) throws OdooCallMethodException {
        try {
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

            return Arrays.stream((Object[]) execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params))
                    .map(o -> (Integer) o)
                    .toArray(Integer[]::new);

        } catch (MalformedURLException | XmlRpcException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     * Search Read
     *
     * @see #searchRead(String, String[], SearchFilter[], String, Integer, Integer)
     *
     * @param model The odoo model
     * @param filters An array of {@link SearchFilter}
     * @param order The order
     * @param offset The offset
     * @param limit The limit
     * @return array of map
     * @throws OdooCallMethodException when calling method failed
     */
    public Map<String, Object>[] searchRead(@Nonnull String model, SearchFilter[] filters, String order, Integer offset, Integer limit) throws OdooCallMethodException {
        return searchRead(model, null, filters, order, offset, limit);
    }

    /**
     * Search Read
     * <p>
     * Implementation of odoo's xmlrpc <b>search_read()</b> method
     *
     * @param model The odoo model
     * @param fields an array of field
     * @param filters An array of {@link SearchFilter}
     * @param order The order
     * @param offset The offset
     * @param limit The limit
     * @return an array of map
     * @throws OdooCallMethodException when calling method failed
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#search-and-read">Search and Read</a>
     */
    @Nonnull
    public Map<String, Object>[] searchRead(@Nonnull String model, String[] fields, SearchFilter[] filters, String order, Integer offset, Integer limit) throws OdooCallMethodException {
        try {
            final Object[] objectFilters = XmlRpcUtil.prefixation(filters);

            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "search_read",
                    new Object[]{objectFilters},
                    new HashMap<String, Object>() {{
                        if (fields != null && fields.length > 0) put("fields", fields);
                        if (offset != null) put("offset", offset);
                        if (limit != null) put("limit", limit);
                        if (order != null) put("order", order);
                    }}
            };

            return Arrays.stream((Object[]) execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params))
                    .map(o -> (Map<String, Object>) o)
                    .peek(m -> m.forEach((key, value) -> {
                        if (value instanceof Boolean && !(boolean) value) m.replace(key, null);
                    }))
                    .toArray(Map[]::new);

        } catch (MalformedURLException | XmlRpcException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     * Search Read
     * <p>
     * Implementation of odoo's xmlrpc <b>search_read()</b> method
     *
     * @param tClass The class that is annotated with {@link OdooModel}
     * @param filters An array of {@link SearchFilter}
     * @param order The order
     * @param offset The offset
     * @param limit The limit
     * @param <T> The type of class
     * @return an array of object
     * @throws OdooCallMethodException when calling method failed
     */
    public <T> T[] searchRead(@Nonnull Class<T> tClass, SearchFilter[] filters, String order, Integer offset, Integer limit) throws OdooCallMethodException {
        String model = getModel(tClass);
        String[] fields = getFields(tClass);
        Map<String, Object>[] records = searchRead(model, fields, filters, order, offset, limit);

        return Arrays.stream(records)
                .filter(Objects::nonNull)
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
     * @param model The odoo model
     * @param filters An array of {@link SearchFilter}
     * @return total of counted record
     * @throws OdooCallMethodException when calling method failed
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#count-records">Count records</a>
     */
    public int searchCount(@Nonnull String model, SearchFilter[] filters) throws OdooCallMethodException {
        try {
            final Object[] objectFilters = XmlRpcUtil.prefixation(filters);

            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "search_count",
                    new Object[]{objectFilters}
            };

            return Optional.ofNullable((Integer) execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params))
                    .orElse(0);

        } catch (MalformedURLException | XmlRpcException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     * Search Count
     *
     * @see #searchCount(String, SearchFilter[])
     *
     * @param tClazz The class that is annotated with {@link OdooModel}
     * @param filters An array of {@link SearchFilter}
     * @return total of counted record
     * @throws OdooCallMethodException when calling method failed
     */
    public int searchCount(@Nonnull Class<?> tClazz, SearchFilter[] filters) throws OdooCallMethodException {
        String model = getModel(tClazz);
        return searchCount(model, filters);
    }


    /**
     * Read
     *
     * @see #read(String, String[], int)
     *
     * @param tClass The class that is annotated with {@link OdooModel}
     * @param recordId The record id
     * @return an optional of map
     * @throws OdooCallMethodException when calling method failed
     */
    public Optional<Map<String, Object>> read(@Nonnull Class<?> tClass, int recordId) throws OdooCallMethodException {
        String model = getModel(tClass);
        String[] fields = getFields(tClass);
        return read(model, fields, recordId);
    }

    /**
     * Read
     *
     * @see #read(String, String[], int)
     *
     * @param model The odoo model
     * @param recordId The record id
     * @return an optional of map
     * @throws OdooCallMethodException when calling method failed
     */
    public Optional<Map<String, Object>> read(String model, int recordId) throws OdooCallMethodException {
        return read(model, null, recordId);
    }

    /**
     * Read
     * <p>
     * Implementation of odoo's xmlrpc <b>read()</b>
     *
     * @param model The odoo model
     * @param fields an array of field
     * @param recordId The record id
     * @return an optional of map
     * @throws OdooCallMethodException when calling method failed
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#read-records">Read records</a>
     */
    public Optional<Map<String, Object>> read(@Nonnull String model, String[] fields, int recordId) throws OdooCallMethodException {
        try {
            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "read",
                    new Object[]{recordId},
                    new HashMap<String, Object>() {{
                        if (fields != null && fields.length > 0) put("fields", fields);
                    }}
            };

            return Arrays.stream((Object[]) execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params))
                    .findFirst()
                    .map(o -> (Map<String, Object>) o)
                    .map(Try.toPeek(m -> m.forEach((key, value) -> {
                        if (value instanceof Boolean && !(boolean) value) m.replace(key, null);
                    })));

        } catch (MalformedURLException | XmlRpcException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     * Create
     * <p>
     * Implementation of odoo's xmlrpc <b>create()</b>
     *
     * @param model The odoo model
     * @param record The record map
     * @return new record id
     * @throws OdooCallMethodException when calling method failed
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#create-records">Create records</a>
     */
    public int create(@Nonnull String model, Map<String, Object> record) throws OdooCallMethodException {
        try {
            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "create",
                    new Object[]{record}
            };

            return (int) execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);

        } catch (MalformedURLException | XmlRpcException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     * Create
     *
     * @see #create(String, Map)
     *
     * @param record The record object
     * @return the new record id
     * @throws OdooCallMethodException when calling method failed
     */
    public <T> int create(@Nonnull T record) throws OdooCallMethodException {
        String model = getModel(record.getClass());
        Map<String, Object> map = getRowMap(record);
        return create(model, map);
    }

    /**
     * Write
     * <p>
     * Implementation of odoo's xmlrpc <b>write()</b>
     *
     * @param model The odoo model
     * @param recordId The record id
     * @param record The record map
     * @throws OdooCallMethodException when calling method failed
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#update-records">Update records</a>
     */
    public void write(@Nonnull String model, int recordId, Map<String, Object> record) throws OdooCallMethodException {
        try {
            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "write",
                    new Object[]{recordId, record},
            };

            execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);

        } catch (MalformedURLException | XmlRpcException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     * Write
     *
     * @see #write(String, int, Map)
     *
     * @param recordId The record id
     * @param record The record object
     * @throws OdooCallMethodException when calling method failed
     */
    public <T> void write(int recordId, @Nonnull T record) throws OdooCallMethodException {
        String model = getModel(record.getClass());
        Map<String, Object> map = getRowMap(record);
        write(model, recordId, map);
    }


    /**
     * Unlink
     * <p>
     * Implementation of odoo's xmlrpc <b>unlink()</b>
     *
     * @param model The odoo model
     * @param recordId The record id
     * @throws OdooCallMethodException when calling method failed
     * @see <a href="https://www.odoo.com/documentation/17.0/developer/reference/external_api.html#delete-records">Delete records</a>
     */
    public void unlink(@Nonnull String model, int recordId) throws OdooCallMethodException {
        try {
            final Object[] params = new Object[]{
                    database,
                    uid,
                    apiKey,
                    model,
                    "unlink",
                    new Object[]{recordId},
            };

            execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);

        } catch (MalformedURLException | XmlRpcException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     * Unlink
     *
     * @see #unlink(String, int)
     *
     * @param tClass The class that is annotated with {@link OdooModel}
     * @param recordId The record id
     * @throws OdooCallMethodException when calling method failed
     */
    public void unlink(@Nonnull Class<?> tClass, int recordId) throws OdooCallMethodException {
        String model = getModel(tClass);
        unlink(model, recordId);
    }

    /**
     * Post message
     *
     * @see #messagePost(String, int[], MessageType, String)
     *
     * @param model The odoo model
     * @param recordId The record id
     * @param body The message body
     * @return new message id
     * @throws OdooCallMethodException when calling method failed
     */
    public int messagePost(@Nonnull String model, int recordId, String body) throws OdooCallMethodException {
        return messagePost(model, new int[]{recordId}, MessageType.COMMENT, body);
    }

    /**
     * Post message
     *
     * @see #messagePost(String, int, String)
     *
     * @param tClass The class that is annotated with {@link OdooModel}
     * @param recordId The record id
     * @param body The message body
     * @return new message id
     * @throws OdooCallMethodException when calling method failed
     */
    public int messagePost(@Nonnull Class<?> tClass, int recordId, String body) throws OdooCallMethodException {
        String model = getModel(tClass);
        return messagePost(model, new int[]{recordId}, MessageType.COMMENT, body);
    }

    /**
     * Post message
     *
     * @param model The odoo model
     * @param recordIds array of record id
     * @param messageType The message type
     * @param body The message body
     * @return new message id
     * @throws OdooCallMethodException when calling method failed
     */
    public int messagePost(@Nonnull String model, int[] recordIds, MessageType messageType, String body) throws OdooCallMethodException {
        try {
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

            return (int) execute(baseUrl + "/" + PATH_OBJECT, "execute_kw", params);
        } catch (MalformedURLException | XmlRpcException e) {
            throw new OdooCallMethodException(e);
        }
    }

    /**
     * Get model name from a class
     * @param tClass The class that is annotated with {@link OdooModel}
     * @return The model name
     * @throws OdooCallMethodException when the class is not annotated with {@link OdooModel}
     */
    @Nonnull
    protected String getModel(@Nonnull Class<?> tClass) throws OdooCallMethodException {
        return Optional.of(tClass)
                .map(c -> c.getAnnotation(OdooModel.class))
                .map(OdooModel::value)
                .orElseThrow(() -> new OdooCallMethodException("Class [" + tClass.getName() + "] is not annotated with @OdooModel"));
    }

    /**
     * Get field names from a class
     * @param tClass The class that is annotated with {@link OdooModel}
     * @return an array of field name
     */
    @Nonnull
    protected String[] getFields(@Nonnull Class<?> tClass) {
        return Optional.of(tClass)
                .map(Class::getDeclaredFields)
                .stream()
                .flatMap(Arrays::stream)
                .map(f -> Optional.of(OdooField.class)
                        .map(f::getAnnotation)
                        .map(OdooField::value)
                        .orElse(f.getName()))
                .toArray(String[]::new);
    }

    /**
     * Get row map from a record object
     * @param record The record object
     * @param <T> The type of record
     * @return The map of the record
     */
    @Nonnull
    protected <T> Map<String, Object> getRowMap(@Nonnull T record) {
        Map<String, Object> map = new HashMap<>();

        Optional.of(record)
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
                }, (Exception ignored) -> {
                    // ignore
                }));

        return map;
    }

    /**
     * Parse record from a map to an object
     * @param tClass The class to be parsed into
     * @param record The map of the record
     * @param <T> The type of the object
     * @return an optional of the object
     */
    protected <T> Optional<T> parseRecord(@Nonnull Class<T> tClass, @Nonnull Map<String, Object> record) {
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

                        if (record.containsValue(fieldName)) {
                            Object value = record.get(fieldName);
                            field.set(instance, value);
                        }
                    }));

            return Optional.of(instance);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    
    /**
     * Execute xml rpc
     * @param url The url
     * @param method The method
     * @param params The parameters
     * @return The result of the execution
     * @throws MalformedURLException when the url is malformed
     * @throws XmlRpcException when the xml rpc execution failed
     */
    protected Object execute(String url, String method, Object[] params) throws MalformedURLException, XmlRpcException {
        return XmlRpcUtil.execute(url, method, params);
    }

    public Object executeKw(String model, String method, Object... args) throws OdooCallMethodException {
        try {
            return XmlRpcUtil.executeKw(baseUrl + "/" + PATH_OBJECT, database, uid, apiKey, model, method, args);
        } catch (MalformedURLException | XmlRpcException e) {
            throw new OdooCallMethodException(e);
        }
    }
}
