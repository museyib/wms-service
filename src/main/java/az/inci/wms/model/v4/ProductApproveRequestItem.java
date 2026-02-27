package az.inci.wms.model.v4;

import lombok.Data;

@Data
public class ProductApproveRequestItem {
    private String invCode;
    private String invName;
    private String invBrand;
    private String barcode;
    private double qty;
}
