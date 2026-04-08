import com.kinnarastudio.odooxmlrpc.annotation.OdooModel;

@OdooModel("product.template")
public class ProductTemplate {
    private double reordering_max_qty;

    public double getReordering_max_qty() {
        return reordering_max_qty;
    }

    public void setReordering_max_qty(double reordering_max_qty) {
        this.reordering_max_qty = reordering_max_qty;
    }

    @Override
    public String toString() {
        return String.valueOf(reordering_max_qty);
    }
}
