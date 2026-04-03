package az.inci.wms.model.v4;

import lombok.Data;

import java.util.List;

@Data
public class ShipmentRequest {
    private String regionCode;
    private String driverCode;
    private String assistantCode;
    private String vehicleCode;
    private String userId;
    private List<ShipmentRequestItem> requestItems;
}
