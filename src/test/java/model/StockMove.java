package model;

import com.kinnarastudio.odooxmlrpc.annotation.OdooField;
import com.kinnarastudio.odooxmlrpc.annotation.OdooModel;

@OdooModel("stock.move")
public class StockMove {
    @OdooField
    private int product_id;

    @OdooField
    private double product_uom_qty;

    @OdooField
    private int product_uom;

    @OdooField
    private int location_id;

    @OdooField
    private int location_dest_id;

    @OdooField
    private String name;

    public int getLocation_id() {
        return location_id;
    }

    public void setLocation_id(int location_id) {
        this.location_id = location_id;
    }

    public int getProduct_uom() {
        return product_uom;
    }

    public void setProduct_uom(int product_uom) {
        this.product_uom = product_uom;
    }

    public double getProduct_uom_qty() {
        return product_uom_qty;
    }

    public void setProduct_uom_qty(double product_uom_qty) {
        this.product_uom_qty = product_uom_qty;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public int getLocation_dest_id() {
        return location_dest_id;
    }

    public void setLocation_dest_id(int location_dest_id) {
        this.location_dest_id = location_dest_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
