package model;

import com.kinnarastudio.odooxmlrpc.annotation.OdooField;
import com.kinnarastudio.odooxmlrpc.annotation.OdooModel;

import java.util.Arrays;
import java.util.stream.Collectors;

@OdooModel("product.template")
public class ProductTemplate {
    @OdooField("reordering_max_qty")
    private double reorderingMaxQty;

    @OdooField("detailed_type")
    private String detailedType;

    public double getReorderingMaxQty() {
        return reorderingMaxQty;
    }

    public void setReorderingMaxQty(double reorderingMaxQty) {
        this.reorderingMaxQty = reorderingMaxQty;
    }

    @Override
    public String toString() {
        return Arrays.stream(getClass().getDeclaredFields()).map(f -> {
            try {
                f.setAccessible(true);
                return f.getName() + "=" + f.get(this);
            } catch (IllegalAccessException e) {
                return f.getName() + "=<error>";
            }
        }).collect(Collectors.joining("; "));
    }

    public String getDetailedType() {
        return detailedType;
    }

    public void setDetailedType(String detailedType) {
        this.detailedType = detailedType;
    }
}
