import com.kinnarastudio.odooxmlrpc.exception.OdooAuthorizationException;
import com.kinnarastudio.odooxmlrpc.exception.OdooCallMethodException;
import com.kinnarastudio.odooxmlrpc.model.Field;
import com.kinnarastudio.odooxmlrpc.model.SearchFilter;
import com.kinnarastudio.odooxmlrpc.rpc.OdooRpc;
import com.kinnarastudio.odooxmlrpc.rpc.SynchronizedOdooRpc;
import com.kinnarastudio.odooxmlrpc.util.XmlRpcUtil;
import model.HrEmployee;
import model.StockMove;
import model.StockPicking;
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
        assert rpc.getUid() == 2;
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
        String model = "hr.employee";
        SearchFilter[] filters = new SearchFilter[]{
                new SearchFilter("barcode", SearchFilter.Operator.NOT_EQUAL, "", SearchFilter.Join.AND),
                new SearchFilter("active", new Object[]{false})
        };
        int count = rpc.searchCount(model, filters);
        System.out.println(count);
    }

    @Test
    public void testSearchRead() throws OdooCallMethodException {
        SearchFilter[] filter = new SearchFilter[]{
                new SearchFilter(SearchFilter.Join.AND, "user_id", SearchFilter.Operator.NOT_EQUAL, null),
                new SearchFilter(SearchFilter.Join.OR, "department_id.name", SearchFilter.Operator.ILIKE, "%Marketing%"),
        };

        HrEmployee[] records = rpc.searchRead(HrEmployee.class, filter, null, null, null);
        System.out.println(records.length);
        Arrays.stream(records)
                .map(m -> {
                    String id = String.valueOf(m.getId());
                    String name = String.valueOf(m.getName());
                    String barcode = String.valueOf(m.getBarcode());
//                    Object[] job_id = (Object[]) m.get("job_id");
//                    String jobId = Arrays.stream(job_id).map(String::valueOf).collect(Collectors.joining(";"));
                    return String.join(" | ", id, name, barcode);
                })
                .map(String::valueOf)
                .forEach(System.out::println);
//                .forEach(System.out::println);

//        Arrays.stream(records).forEach(System.out::println);
    }

    @Test
    public void testFieldsGet() throws OdooCallMethodException {
//        final Collection<Field> fields = rpc.fieldsGet(HrEmployee.class);
        final Collection<Field> fields = rpc.fieldsGet("hr.employee");

        assert !fields.isEmpty();

        fields.forEach((f) -> {
            System.out.println("[" + f + "]");
//            f.getMetadata().forEach((k2, v2) -> System.out.println(k2 + "->" + v2));

            System.out.println(f.getType() + "->" + f.getKey());
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
        Map<String, Object> record = rpc.searchRead(model, null, null, null, 4)[0];
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

    @org.junit.Test
    public void testReadGroup() throws OdooAuthorizationException, OdooCallMethodException {
        final OdooRpc rpc = new OdooRpc(baseUrl, database, user, apiKey);

//        final String[] fields = new String[]{"department_id", "child_all_count"};
        final String[] fields = new String[] {"contracts_count:avg"};
        final String[] groups = new String[] {"department_id"};

//        SearchFilter[] filters = new SearchFilter[] { new SearchFilter("department_id", 307) };
        SearchFilter[] filters = null;

        Map<String, Object>[] result = rpc.readGroup("hr.employee", fields, groups, filters);
        System.out.println("Total group yang ditemukan: " + result.length);

        for (Object r : result) {
            Map<String, Object> record = (Map<String, Object>) r;

            System.out.println(record.keySet().stream().collect(Collectors.joining(";")));

            // Mengambil nilai department_id dan mem-formatnya (bisa Object[] [id, "Nama Dept"] atau boolean false)
            Object dept = record.get("company_id");
            String deptStr = (dept instanceof Object[]) ? Arrays.toString((Object[]) dept) : String.valueOf(dept);

            System.out.println("Department: " + deptStr + " | Total Karyawan: " + record.get("__count"));
        }
    }

    @org.junit.Test
    public void testReadGroup2() throws OdooAuthorizationException, OdooCallMethodException {
        final OdooRpc rpc = new OdooRpc(baseUrl, database, user, apiKey);

        // Domain filter untuk 1 September - 30 September 2026
        Object[] domain = new Object[]{
                new Object[]{"create_date", ">=", "2026-09-01 00:00:00"},
                new Object[]{"create_date", "<=", "2026-09-30 23:59:59"}
        };

        // Gunakan read_group dengan syntax aggregasi "field:operator"
        // Operator yang tersedia biasanya: sum, avg, max, min
        Map<String, Object> kwargs = new HashMap<>();
        kwargs.put("fields", new String[]{"amount_total:avg"});

        // Group by dikosongkan [] agar Odoo menghitung agregasi seluruh data (tanpa dibagi-bagi per kelompok)
        // Catatan: Jika versi Odoo yang Anda pakai menolak groupby kosong, Anda bisa isi dengan "create_date:month"
        kwargs.put("groupby", new Object[]{});

        // Eksekusi method read_group pada model sale.order
        Object[] result = (Object[]) rpc.executeKw("sale.order", "read_group", new Object[]{ domain }, kwargs);

        for (Object r : result) {
            Map<String, Object> record = (Map<String, Object>) r;

            // Odoo akan memasukkan hasil rata-ratanya ke dalam key sesuai nama field aslinya
            Object average = record.get("amount_total");
            Object count = record.get("__count"); // Odoo biasanya mengembalikan jumlah baris di dalam key __count

            System.out.println("Total Quotation: " + count);
            if (average instanceof Number) {
                System.out.printf("Rata-rata Harga: Rp %,.2f%n", ((Number) average).doubleValue());
            } else {
                System.out.println("Rata-rata Harga: " + average);
            }
        }
    }

    @Test
    public void testCallingMethod() throws OdooAuthorizationException, OdooCallMethodException {
        Object[] args = new Object[]{529};
        Object result = rpc.executeKw("item.request", "save_approver_in_list", 529);
        System.out.println("Result: " + result);
    }

    @org.junit.Test
    public void testCreateStockPicking() throws OdooCallMethodException {
        // 1. Siapkan List of Map untuk line item (stock.move)

        // 2. Ubah data list menjadi Array of Command Odoo untuk relasi One2many
//        Object[] moveCommands = new Object[stockMoveDataList.size()];
//        for (int i = 0; i < stockMoveDataList.size(); i++) {
//            // Bungkus dengan tuple (0, 0, {values}) untuk create new record pada line item
//            moveCommands[i] = new Object[]{0, 0, stockMoveDataList.get(i)};
//        }

        // 3. Data utama untuk stock.picking
        final StockPicking stockPickingData = new StockPicking();
        stockPickingData.setPicking_type_id(33); // PT. PENJALINDO NUSANTARA: Internal Request PENJALINDO
        stockPickingData.setLocation_id(185); // PT. P/Stok/WH-HRGA
        stockPickingData.setGoods_withdrawal_categories(6); // Material request ATK Pabrik

        StockMove stockMove = new StockMove();
        stockMove.setProduct_id(292); // ATK0000275
        stockMove.setName("Test");
        stockMove.setProduct_uom_qty(2.0);
        stockMove.setProduct_uom(1); // Units
        stockMove.setLocation_id(185); // PT. P/Stok/WH-HRGA
        stockMove.setLocation_dest_id(185);
        StockMove[] stockMoveList = new StockMove[]{stockMove};

//        stockPickingData.setMove_ids(stockMoveList);

        // 4. Eksekusi request ke Odoo
        int pickingId = rpc.create(stockPickingData);

        System.out.println("ID Stock Picking yang berhasil dibuat: " + pickingId);

        rpc.read("stock.picking", pickingId)
                .map(Map::entrySet)
                .stream()
                .flatMap(Collection::stream)
                .map(e -> {
                    Object value = e.getValue();
                    if (value instanceof Object[]) {
                        return e.getKey() + "->" + Arrays.stream((Object[]) value).map(String::valueOf).collect(Collectors.joining(";"));
                    } else {
                        return e.getKey() + "->" + value;
                    }
                })
                .forEach(System.out::println);
    }

    @Test
    public void testStockPickingRead() throws OdooCallMethodException {
//        rpc.read(StockPicking.class, 929)
//                .map(StockPicking::getMove_ids)
//                .stream()
//                .flatMap(Arrays::stream)
//                .map(StockMove::getName)
//                .forEach(System.out::println);
//
//        rpc.read(StockMove.class, 15310)
//                .map(StockMove::getName)
//                .ifPresent(System.out::println);


        System.out.println("=========================");

//        rpc.read(StockPicking.MODEL, 928)
//                .map(Map::entrySet)
//                .stream()
//                .flatMap(Collection::stream)
//                .map(e -> {
//                    if(e.getValue() instanceof Object[]) {
//                        return e.getKey() + "->" + e.getValue();
//                    } else {
//                        return e.getKey() + "->" + e.getValue();
//                    }
//                })
//                .forEach(System.out::println);
//
//        System.out.println("=========================");

        rpc.read(StockPicking.MODEL, 969)
                .map(Map::entrySet)
                .stream()
                .flatMap(Collection::stream)
                .map(e -> {
                    if (e.getValue() instanceof Object[]) {
                        return e.getKey() + "->" + Arrays.stream(((Object[]) e.getValue())).map(String::valueOf).collect(Collectors.joining(";"));
                    } else {
                        return e.getKey() + "->" + e.getValue();
                    }
                })
                .forEach(System.out::println);
        System.out.println("--------------------------------------");
        rpc.read(StockMove.MODEL, 15433)
                .map(Map::entrySet)
                .stream()
                .flatMap(Collection::stream)
                .map(e -> {
                    if (e.getValue() instanceof Object[]) {
                        return e.getKey() + "->" + Arrays.stream(((Object[]) e.getValue())).map(String::valueOf).collect(Collectors.joining(";"));
                    } else {
                        return e.getKey() + "->" + e.getValue();
                    }
                })
                .forEach(System.out::println);
        System.out.println("--------------------------------------");
//        rpc.read(StockMove.MODEL, 15430)
//                .map(Map::entrySet)
//                .stream()
//                .flatMap(Collection::stream)
//                .map(e -> {
//                    if (e.getValue() instanceof Object[]) {
//                        return e.getKey() + "->" + Arrays.stream(((Object[]) e.getValue())).map(String::valueOf).collect(Collectors.joining(";"));
//                    } else {
//                        return e.getKey() + "->" + e.getValue();
//                    }
//                })
//                .forEach(System.out::println);

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
