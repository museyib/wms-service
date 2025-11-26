package az.inci.wms.controller.v4;

import az.inci.wms.model.v4.*;
import az.inci.wms.services.v4.InventoryServiceV4;
import az.inci.wms.services.v4.UserServiceV4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RequestMapping("/v4/inv")
@RestController
public class InventoryControllerV4 {
    private InventoryServiceV4 service;
    private UserServiceV4 userService;

    @Autowired
    public void setService(InventoryServiceV4 service) {
        this.service = service;
    }

    @Autowired
    public void setUserService(UserServiceV4 userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/qty", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<BigDecimal>> getQty(@RequestParam("whs-code") String whsCode,
                                                       @RequestParam("inv-code") String invCode) {
        BigDecimal result = service.getQty(whsCode, invCode);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/info-by-barcode", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<Inventory>> getInfoByBarcode(@RequestParam("barcode") String barcode,
                                                              @RequestParam("user-id") String userId) {
        Inventory result = service.getInfoByBarcode(barcode, userId);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/info-by-inv-code", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<Inventory>> getInfoByInvCode(@RequestParam("inv-code") String invCode,
                                                              @RequestParam("user-id") String userId) {
        Inventory result = service.getInfoByInvCode(invCode, userId);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/search", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<List<Inventory>>> getSearchResult(@RequestParam("keyword") String keyword,
                                                                     @RequestParam("in") String field) {
        List<Inventory> result = service.getSearchResult(keyword, field);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/by-barcode", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<Inventory>> getInvByBarcode(@RequestParam("barcode") String barcode) {
        Inventory result = service.getInvByBarcode(barcode);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/pick-report", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<Integer>> getPickReport(@RequestParam("start-date") String startDate,
                                                           @RequestParam("end-date") String endDate,
                                                           @RequestParam("user-id") String pickUser) {
        Integer result = service.getPickReport(startDate, endDate, pickUser);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/pack-report", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<Integer>> getPackReport(@RequestParam("start-date") String startDate,
                                                           @RequestParam("end-date") String endDate,
                                                           @RequestParam("user-id") String approveUser) {
        Integer result = service.getPackReport(startDate, endDate, approveUser);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/attribute-list", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<List<InvAttribute>>> getAttributeList(@RequestParam("inv-code") String invCode) {
        List<InvAttribute> result = service.getAttributeList(invCode);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/attribute-list-by-whs", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<List<InvAttribute>>> getAttributeListByWhs(@RequestParam("inv-code") String invCode,
                                                                              @RequestParam("user-id") String userId) {
        List<InvAttribute> result = service.getAttributeList(invCode, userId);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/barcode-list", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<List<InvBarcode>>> getBarcodeList(@RequestParam("inv-code") String invCode) {
        List<InvBarcode> result = service.getBarcodeList(invCode);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @PostMapping(value = "/update-attributes", consumes = "application/json;charset=UTF-8")
    public ResponseEntity<Response<Void>> updateInvAttributes(@RequestBody List<InvAttribute> attributeList) {
        service.updateInvAttributes(attributeList);
        return ResponseEntity.ok(Response.getSuccessResponse());
    }

    @PostMapping(value = "/update-shelf-barcode", consumes = "application/json;charset=UTF-8")
    public ResponseEntity<Response<Void>> updateShelfBarcode(@RequestParam("whs-code") String whsCode,
                                                             @RequestParam("shelf-barcode") String shelfBarcode,
                                                             @RequestBody List<String> invBarcodeList) {
        for (String invBarcode : invBarcodeList) {
            boolean result = service.updateShelfBarcode(whsCode, shelfBarcode, invBarcode);
            if (!result)
                return ResponseEntity.ok(Response.getUserErrorResponse("Barkod üzrə mal tapılmadı: " + invBarcode));
        }
        return ResponseEntity.ok(Response.getSuccessResponse());
    }

    @PostMapping(value = "/update-barcodes", consumes = "application/json;charset=UTF-8")
    public ResponseEntity<Response<Void>> updateInvBarcodes(@RequestBody Request<List<InvBarcode>> request) {
        boolean permissionGranted;

        User user = userService.getById(request.getUserId());
        permissionGranted = user.isBarcodeFlag();

        if (permissionGranted)
            service.updateInvBarcodes(request.getData());
        else
            return ResponseEntity.ok(Response.getUserErrorResponse("Səlahiyyətiniz yoxdur."));

        return ResponseEntity.ok(Response.getSuccessResponse());
    }

    @PostMapping(value = "/update-barcode", consumes = "application/json;charset=UTF-8")
    public ResponseEntity<Response<Void>> updateInvBarcode(@RequestBody Request<List<InvBarcode>> request) {
        boolean permissionGranted;

        User user = userService.getById(request.getUserId());
        permissionGranted = user.isBarcodeFlag();

        if (permissionGranted)
            service.updateInvBarcodes(request.getData());
        else
            return ResponseEntity.ok(Response.getUserErrorResponse("Səlahiyyətiniz yoxdur."));

        return ResponseEntity.ok(Response.getSuccessResponse());
    }

    @GetMapping(produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<List<Inventory>>> getInvList() {
        List<Inventory> result = service.getInvList();
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/by-user-producer-list", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<List<Inventory>>> getInvListByUser(@RequestParam("user-id") String userId) {
        List<Inventory> result = service.getInvListByUser(userId);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/whs-sum", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<List<Inventory>>> getWhsSumByUser(@RequestParam("user-id") String userId,
                                                                     @RequestParam("whs-code") String whsCode) {
        List<Inventory> result = service.getWhsSumByUser(userId, whsCode);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/inv-barcode", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<InvBarcode>> getInvBarcode(@RequestParam("barcode") String barcode) {
        InvBarcode result = service.getInvBarcode(barcode);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/latest-movements", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<List<LatestMovementItem>>> getLatestMovements(@RequestParam("inv-code") String invCode,
                                                                                 @RequestParam("whs-code") String whsCode,
                                                                                 @RequestParam("top") int top) {
        List<LatestMovementItem> result = service.getLatestMovementItems(invCode, whsCode, top);
        return ResponseEntity.ok(Response.getResultResponse(result));
    }

    @GetMapping(value = "/brands", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Response<List<Brand>>> getBrandList() {
        List<Brand> result = service.getBrandList();
        return ResponseEntity.ok(Response.getResultResponse(result));
    }


    @PostMapping(value = "/update-inv-master-data", consumes = "application/json;charset=UTF-8")
    public ResponseEntity<Response<Void>> updateInvMasterData(@RequestBody UpdateInvRequest request) {
        service.updateInvMasterData(request);
        return ResponseEntity.ok(Response.getSuccessResponse());
    }
}
