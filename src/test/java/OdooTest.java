import com.kinnarastudio.odooxmlrpc.exception.OdooAuthorizationException;
import com.kinnarastudio.odooxmlrpc.exception.OdooCallMethodException;
import com.kinnarastudio.odooxmlrpc.model.Field;
import com.kinnarastudio.odooxmlrpc.model.SearchFilter;
import com.kinnarastudio.odooxmlrpc.rpc.OdooRpc;
import com.kinnarastudio.odooxmlrpc.rpc.SynchronizedOdooRpc;
import com.kinnarastudio.odooxmlrpc.rpc.XmlRpcUtil;
import model.HrEmployee;
import model.ProductTemplate;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class OdooTest {
    public final static String PROPERTIES_FILE = "test.properties";
    private final String baseUrl;
    private final String database;

    private final String user;

    private final String apiKey;

    private final OdooRpc rpc;

    public OdooTest() throws OdooAuthorizationException {
        final Properties properties = getProperties(PROPERTIES_FILE);
        baseUrl = properties.get("baseUrl").toString();
        database = properties.get("database").toString();
        user = properties.get("user").toString();
        apiKey = properties.get("apiKey").toString();

        rpc = new SynchronizedOdooRpc(baseUrl, database, user, apiKey);
    }

    @Test
    public void testLogin() throws OdooAuthorizationException {
        final OdooRpc rpc = new OdooRpc(baseUrl, database, user, apiKey);
        int uid = rpc.login();
        assert uid == 2;
    }

    @Test
    public void testSearch() throws OdooCallMethodException, OdooAuthorizationException {
        final OdooRpc rpc = new OdooRpc(baseUrl, database, user, apiKey);

        final Collection<Integer> records = new HashSet<>();
        String model = "product.template";

//        SearchFilter[] filters = SearchFilter.single("movement_id", 9);
        SearchFilter[] filters = new SearchFilter[]{
                new SearchFilter("id", 107),
                new SearchFilter(SearchFilter.Join.OR, "id", 108),
                new SearchFilter(SearchFilter.Join.OR, "id", 71),
                new SearchFilter("name", SearchFilter.Operator.ILIKE, "BOX")
        };
        for (Map<String, Object> record : rpc.searchRead(model, filters, null, null, null)) {
            System.out.println(record.entrySet().stream().map(e -> e.getKey() + "->" + e.getValue()).collect(Collectors.joining(" | ")));
        }
    }

    @Test
    public void testSearchWithClass() throws OdooCallMethodException, OdooAuthorizationException {
        final OdooRpc rpc = new OdooRpc(baseUrl, database, user, apiKey);

        final Collection<Integer> records = new HashSet<>();

//        SearchFilter[] filters = SearchFilter.single("movement_id", 9);
        SearchFilter[] filters = new SearchFilter[]{
//                new SearchFilter( "id", 107),
//                new SearchFilter(SearchFilter.Join.OR, "id", 108),
//                new SearchFilter(SearchFilter.Join.OR, "id", 71),
                new SearchFilter("uom_category_id", SearchFilter.Operator.NOT_EQUAL, null)
        };
        for (Map<String, Object> record : rpc.searchRead("product.template", filters, null, null, null)) {
            System.out.println(record.toString());
        }
    }

    @Test
    public void testRead() throws OdooCallMethodException {
        String model = "hr.employee";
        SearchFilter[] filter = null;
        int recordId = rpc.search(HrEmployee.class, filter, null, null, 4)[0];
        final Map<String, Object>[] records = rpc.read(model, new int[]{recordId});

        Arrays.stream(records)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .map(e -> e.getKey() + "->" + e.getValue())
                .forEach(System.out::println);
    }

    @Test
    public void testSearchCount() throws OdooCallMethodException {
        SearchFilter[] filter = new SearchFilter[]{
                new SearchFilter("job_id", SearchFilter.Operator.EQUAL, 515)
        };
        int count = rpc.searchCount(HrEmployee.class, filter);
        System.out.println(count);
    }

    @Test
    public void testSearchRead() throws OdooCallMethodException {
        String model = "hr.employee";
        String[] fields = new String[]{"id", "name", "barcode", "job_id"};
        SearchFilter[] filter = SearchFilter.getBuilder()
//                .and("job_id", 514)
                .or("job_id", SearchFilter.Operator.LESS, 515)
                .build();

//        SearchFilter[] filter = SearchFilter.eq("id", 1380);
//        Map<String, Object>[] records = rpc.searchRead(model, fields, filter, null, null, null);

        Arrays.stream(rpc.searchRead(HrEmployee.class, filter, null, null, 2))
                .map(m -> {
                    String id = String.valueOf(m.getId());
                    String name = String.valueOf(m.getName());
                    String barcode = String.valueOf(m.getBarcode());
//                    Object[] job_id = (Object[]) m.get("job_id");
//                    String jobId = Arrays.stream(job_id).map(String::valueOf).collect(Collectors.joining(";"));
                    return String.join(" | ", id,name, barcode);
                })
                .map(String::valueOf)
                .forEach(System.out::println);
//                .forEach(System.out::println);

//        Arrays.stream(records).forEach(System.out::println);
    }

    @Test
    public void testFieldsGet() throws OdooCallMethodException {
        final Collection<Field> fields = rpc.fieldsGet(ProductTemplate.class);

        assert !fields.isEmpty();

        fields.forEach((f) -> {
            System.out.println("[" + f + "]");
            f.getMetadata().forEach((k2, v2) -> System.out.println(k2 + "->" + v2));
        });
    }

    @Test
    public void testWrite() throws OdooCallMethodException {
        String model = "stock.movements";
        SearchFilter[] filter = new SearchFilter[]{
                new SearchFilter("name", "PB00010")
        };
        int recordId = rpc.search(model, filter, null, null, 4)[0];

        rpc.write(model, recordId, new HashMap<>() {{
            put("goods_withdrawal_categories", 1);
        }});
    }

    @Test
    public void testCreate() throws OdooCallMethodException {
        String model = "stock.movements";
        SearchFilter[] filter = new SearchFilter[]{
                new SearchFilter("name", "PB00010")
        };
        Map<String, Object> record = rpc.searchRead(model, filter, null, null, 4)[0];
        int recordId = rpc.create(model, record);

        System.out.println(recordId);
    }

    @Test
    public void testDelete() throws OdooCallMethodException {
        String model = "product.pricelist";
        int[] recordId = rpc.search(model, null, null, null, 4);
        rpc.unlink(model, recordId[0]);
    }

    protected Properties getProperties(String file) {
        Properties prop = new Properties();
        try (InputStream inputStream = OdooTest.class.getResourceAsStream(file)) {
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return prop;
    }

    @Test
    public void test() {
        System.out.println(Arrays.stream(new String[0]).anyMatch(String::isEmpty));
    }

    @Test
    public void testBus() throws OdooCallMethodException {
        int messageId = rpc.messagePost("purchase.order", 1, "Sending from kecak [" + new Date() + "]");
    }

    @Test
    public void testCreatePricelist() throws OdooCallMethodException {
        String model = "product.pricelist";
        final Map<String, Object> record = new HashMap<>() {{
            put("name", "Test");
            put("currency_id", 1);
            put("company_id", false);
        }};

        int recordId = rpc.create(model, record);

        System.out.println(recordId);
    }

    @org.junit.Test
    public void testCreateProductTemplate() throws OdooCallMethodException {

        String model = "product.template";
        final Map<String, Object> record = new HashMap<>() {{
            put("name", "Testing Product Hqhr");
            put("customer_id", 1416);
            put("categ_id", 595);
            put("list_price", 1.0);
            put("size_fw", 80);
            put("size_sw", 0.0);
            put("size_pitch", 190);
            put("spec_length", 1000);
            put("spec_thickness", 35);
            put("material_film_id", 6750);
            put("uom_id", 1);
            put("uom_po_id", 27);
            put("purchase_line_warn", "no-message");
            put("sale_line_warn", "no-message");
            put("tracking", "none");
            put("detailed_type", "product");
        }};

        int recordId = rpc.create(model, record);

        System.out.println(recordId);
    }

    @Test
    public void prefixization() {
        Object[] result = XmlRpcUtil.prefixation(new SearchFilter[]{
                new SearchFilter(SearchFilter.Join.OR, "id", 1),
                new SearchFilter(SearchFilter.Join.OR, "id", 2),
                new SearchFilter(SearchFilter.Join.AND, "id", 2),
        });

        for (Object o : result) {
            System.out.println(o);
        }
    }


    @Test
    public void searchPurchaseRequest() {
        try {
            String model = "purchase.request";
            SearchFilter[] filters = new SearchFilter[]{
                    new SearchFilter("id", 73)
            };
            for (Map<String, Object> record : rpc.searchRead(model, filters, null, null, null)) {
                System.out.println(record.entrySet().stream().map(e -> e.getKey() + "->" + e.getValue()).collect(Collectors.joining(" | ")));
            }
        } catch (OdooCallMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void rejectPurchaseRequest() {
        try {
            String model = "purchase.request";

            rpc.write(model, 73, new HashMap<>() {{
                put("name", "rejected");
            }});

        } catch (OdooCallMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void executeKw() {
        try {
            String model = "item.request";

            Object result = rpc.executeKw(model, "get_user_warehouse_per_category", 1, 2, 3, new HashMap<>() {{
                put("id", 1);
            }});

            System.out.println("Result: " + result);
        } catch (OdooCallMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCallingMethod() throws OdooAuthorizationException, OdooCallMethodException {
        Object[] args = new Object[] {529};
            Object result = rpc.executeKw("item.request", "save_approver_in_list", 529);
        System.out.println("Result: " + result);
    }

    /**
     * Baca dari DB
     *
     * HOD / Depart / Valid
     *
     *
     *
     * Username
     *
     * Cek ke odoo username (barcode_id = username, delegate = true, start date >= now, end date <= now) => delegate_employee_id
     *
     * API delegate_employee_id => username
     *
     */
}
