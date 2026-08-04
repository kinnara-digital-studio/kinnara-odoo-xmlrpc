package model;

import com.kinnarastudio.odooxmlrpc.annotation.OdooField;
import com.kinnarastudio.odooxmlrpc.annotation.OdooModel;

@OdooModel("stock.picking")
public class StockPicking {
    @OdooField
    private int picking_type_id;

    @OdooField
    private int location_id;

    @OdooField
    private int goods_withdrawal_categories;

    @OdooField
    private String state;

    @OdooField("move_ids")
    private StockMove[] moveIds;

    public int getPicking_type_id() {
        return picking_type_id;
    }

    public int getLocation_id() {
        return location_id;
    }

    public String getState() {
        return state;
    }

    public StockMove[] getMove_ids() {
        return moveIds;
    }

    public void setPicking_type_id(int picking_type_id) {
        this.picking_type_id = picking_type_id;
    }

    public void setLocation_id(int location_id) {
        this.location_id = location_id;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setMove_ids(StockMove[] moveIds) {
        this.moveIds = moveIds;
    }

    public int getGoods_withdrawal_categories() {
        return goods_withdrawal_categories;
    }

    public void setGoods_withdrawal_categories(int goods_withdrawal_categories) {
        this.goods_withdrawal_categories = goods_withdrawal_categories;
    }
}
