package az.inci.wms.services.v4;


import az.inci.wms.model.v4.*;
import az.inci.wms.services.AbstractService;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.ParameterMode.IN;

@Service
public class InventoryServiceV4 extends AbstractService {
    public Inventory getInfoByBarcode(String barcode, String userId) {
        Inventory inventory = new Inventory();

        StoredProcedureQuery q = em.createStoredProcedureQuery("SP_INV_INFO_BY_BARCODE");
        q.registerStoredProcedureParameter("BARCODE", String.class, IN);
        q.registerStoredProcedureParameter("USER_ID", String.class, IN);
        q.setParameter("BARCODE", barcode);
        q.setParameter("USER_ID", userId);
        List<Object[]> result = q.getResultList();
        if (!result.isEmpty()) {
            inventory.setInvCode(String.valueOf(result.get(0)[0]));
            inventory.setInvName(String.valueOf(result.get(0)[1]));
            inventory.setInfo(String.valueOf(result.get(0)[2]));
            inventory.setWhsQty(Double.parseDouble(String.valueOf(result.get(0)[3])));
            inventory.setDefaultUomCode(String.valueOf(result.get(0)[4]));
            inventory.setWhsCode(String.valueOf(result.get(0)[5]));
            inventory.setInvBrand(String.valueOf(result.get(0)[6]));
        }

        em.close();

        return inventory;
    }

    public Inventory getInfoByInvCode(String invCode, String userId) {
        Inventory inventory = new Inventory();

        StoredProcedureQuery q = em.createStoredProcedureQuery("SP_INV_INFO_BY_INV_CODE");
        q.registerStoredProcedureParameter("INV_CODE", String.class, IN);
        q.registerStoredProcedureParameter("USER_ID", String.class, IN);
        q.setParameter("INV_CODE", invCode);
        q.setParameter("USER_ID", userId);
        List<Object[]> result = q.getResultList();
        if (!result.isEmpty()) {
            inventory.setInvCode(String.valueOf(result.get(0)[0]));
            inventory.setInvName(String.valueOf(result.get(0)[1]));
            inventory.setInfo(String.valueOf(result.get(0)[2]));
            inventory.setWhsQty(Double.parseDouble(String.valueOf(result.get(0)[3])));
            inventory.setDefaultUomCode(String.valueOf(result.get(0)[4]));
            inventory.setWhsCode(String.valueOf(result.get(0)[5]));
            inventory.setInvBrand(String.valueOf(result.get(0)[6]));
        }

        em.close();

        return inventory;
    }

    public List<Inventory> getSearchResult(String keyword, String field) {
        List<Inventory> inventoryList = new ArrayList<>();

        String wildCardKeyword = "%".concat(keyword.trim().replaceAll(" ", "%")).concat("%");

        Query q = em.createNativeQuery(
                "SELECT INV_CODE, INV_NAME, INV_BRAND_CODE, UNIT_CODE FROM INV_MASTER WHERE INV_NAME LIKE :KEYWORD");
        if (field != null) {
            switch (field) {
                case "Kod" -> q = em.createNativeQuery(
                        "SELECT INV_CODE, INV_NAME, INV_BRAND_CODE, UNIT_CODE FROM INV_MASTER WHERE INV_CODE LIKE :KEYWORD");
                case "Ad" -> q = em.createNativeQuery(
                        "SELECT INV_CODE, INV_NAME, INV_BRAND_CODE, UNIT_CODE FROM INV_MASTER WHERE INV_NAME LIKE :KEYWORD");
            }
        }

        q.setParameter("KEYWORD", wildCardKeyword);

        List<Object[]> resultList = q.getResultList();
        resultList.stream().map((result) ->
        {
            Inventory inventory = new Inventory();
            inventory.setInvCode(String.valueOf(result[0]));
            inventory.setInvName(String.valueOf(result[1]));
            inventory.setInvBrand(String.valueOf(result[2]));
            inventory.setDefaultUomCode(String.valueOf(result[3]));
            return inventory;
        }).forEachOrdered(inventoryList::add);

        em.close();

        return inventoryList;
    }

    public Inventory getInvByBarcode(String barcode) {
        Query q = em.createNativeQuery("""
                SELECT IM.INV_CODE,
                    IM.INV_NAME,
                    IM.INV_BRAND_CODE,
                    PL.PRICE
                FROM INV_MASTER IM
                LEFT JOIN INV_BARCODE IB ON IM.INV_CODE = IB.INV_CODE
                LEFT JOIN PRICE_LIST PL ON IM.INV_CODE = PL.INV_CODE
                                             AND PL.PRICE_CODE = 'P01'
                WHERE IB.BAR_CODE = :BAR_CODE""");
        q.setParameter("BAR_CODE", barcode);
        List<Object[]> resultList = q.getResultList();
        Inventory inventory = new Inventory();
        inventory.setBarcode(barcode);
        resultList.stream().peek((result) -> inventory.setInvCode(String.valueOf(result[0])))
                .peek((result) -> inventory.setInvName(String.valueOf(result[1])))
                .peek((result) -> inventory.setInvBrand(String.valueOf(result[2])))
                .forEachOrdered((result) -> inventory.setPrice(Double.parseDouble(String.valueOf(result[3]))));

        em.close();

        return inventory;
    }

    public BigDecimal getQty(String whsCode, String invCode) {
        Query q = em.createNativeQuery("""
                SELECT WHS_QTY FROM WHS_SUM
                WHERE WHS_CODE = :WHS_CODE AND INV_CODE = :INV_CODE""");
        q.setParameter("WHS_CODE", whsCode);
        q.setParameter("INV_CODE", invCode);
        List<BigDecimal> resultList = q.getResultList();
        BigDecimal qty = new BigDecimal("0");
        if (!resultList.isEmpty())
            qty = resultList.get(0);

        em.close();

        return qty;
    }

    public Integer getPickReport(String startDate, String endDate, String pickUser) {
        Query q = em.createNativeQuery("""
                SELECT count(*) FROM INV_PICK_TRX IPT JOIN PICK_DOC PD
                ON IPT.TRX_NO=PD.TRX_NO AND PD.REC_STATUS=6
                WHERE IPT.TRX_DATE BETWEEN :START_DATE AND :END_DATE
                AND IPT.PICK_USER_ID = :USER_ID
                AND IPT.PICK_STATUS IN ('P', 'Q')""");
        q.setParameter("START_DATE", startDate);
        q.setParameter("END_DATE", endDate);
        q.setParameter("USER_ID", pickUser);
        List<Integer> resultList = q.getResultList();
        Integer qty = 0;
        if (!resultList.isEmpty())
            qty = resultList.get(0);

        em.close();

        return qty;
    }

    public Integer getPackReport(String startDate, String endDate, String approveUser) {
        Query q = em.createNativeQuery("""
                SELECT count(*) FROM INV_PICK_TRX IPT
                JOIN PICK_DOC PD ON IPT.TRX_NO=PD.TRX_NO
                WHERE IPT.TRX_DATE BETWEEN :START_DATE AND :END_DATE
                    AND IPT.APPROVE_USER_ID = :USER_ID
                    AND IPT.PICK_STATUS IN ('P', 'Q')""");
        q.setParameter("START_DATE", startDate);
        q.setParameter("END_DATE", endDate);
        q.setParameter("USER_ID", approveUser);
        List<Integer> resultList = q.getResultList();
        Integer qty = 0;
        if (!resultList.isEmpty())
            qty = resultList.get(0);

        em.close();

        return qty;
    }

    public List<InvAttribute> getAttributeList(String invCode) {
        List<InvAttribute> attributeList = new ArrayList<>();

        Query q = em.createNativeQuery("""
                SELECT IMA.INV_CODE,
                    IMA.INV_ATTRIB_ID,
                    IMA.INV_ATTRIB_NAME,
                    IMA.DATA_TYPE,
                    ISNULL(IA.ATTRIB_VALUE, '') AS ATTRIB_VALUE,
                    IA.WHS_CODE,
                    IIF(IA.ATTRIB_VALUE IS NULL, 0, 1) ATTRIB_STATUS
                FROM (SELECT IM.INV_CODE,
                           IAD.INV_ATTRIB_ID,
                           IAD.INV_ATTRIB_NAME,
                           IAD.DATA_TYPE
                    FROM INV_MASTER IM,
                         INV_ATTRIB_DEF IAD) IMA
                LEFT JOIN INV_ATTRIB IA ON IA.INV_CODE = IMA.INV_CODE
                                         AND IA.INV_ATTRIB_ID = IMA.INV_ATTRIB_ID
                WHERE IMA.INV_CODE = :INV_CODE""");
        q.setParameter("INV_CODE", invCode);
        List<Object[]> resultList = q.getResultList();
        resultList.stream().map((result) ->
        {
            InvAttribute attribute = new InvAttribute();
            attribute.setInvCode(String.valueOf(result[0]));
            attribute.setAttributeId(String.valueOf(result[1]));
            attribute.setAttributeName(String.valueOf(result[2]));
            attribute.setAttributeType(String.valueOf(result[3]));
            attribute.setAttributeValue(String.valueOf(result[4]));
            attribute.setWhsCode(String.valueOf(result[5]));
            attribute.setDefined(Integer.parseInt(String.valueOf(result[6])) == 1);
            return attribute;
        }).forEachOrdered(attributeList::add);

        em.close();

        return attributeList;
    }

    public List<InvAttribute> getAttributeList(String invCode, String userId) {
        List<InvAttribute> attributeList = new ArrayList<>();

        Query q = em.createNativeQuery("""
                SELECT IMA.INV_CODE,
                    IMA.INV_ATTRIB_ID,
                    IMA.INV_ATTRIB_NAME,
                    IMA.DATA_TYPE,
                    ISNULL(IA.ATTRIB_VALUE, '') AS ATTRIB_VALUE,
                    IIF(IA.WHS_CODE IS NULL OR IA.WHS_CODE='',
                        (SELECT TOP 1 WHS_CODE
                        FROM BMS_USER_WHS
                        WHERE USER_ID = :USER_ID), IA.WHS_CODE) AS WHS_CODE,
                    IIF(IA.ATTRIB_VALUE IS NULL, 0, 1) ATTRIB_STATUS
                FROM (SELECT IM.INV_CODE,
                           IAD.INV_ATTRIB_ID,
                           IAD.INV_ATTRIB_NAME,
                           IAD.DATA_TYPE
                    FROM INV_MASTER IM,
                         INV_ATTRIB_DEF IAD) IMA
                LEFT JOIN INV_ATTRIB IA ON IA.INV_CODE = IMA.INV_CODE
                                    AND IA.INV_ATTRIB_ID = IMA.INV_ATTRIB_ID
                                    AND (IA.WHS_CODE = (SELECT TOP 1 WHS_CODE
                                                         FROM BMS_USER_WHS
                                                         WHERE USER_ID = :USER_ID)
                                             OR IA.INV_ATTRIB_ID NOT IN ('AT010','AT011'))
                WHERE IMA.INV_CODE = :INV_CODE""");
        q.setParameter("USER_ID", userId);
        q.setParameter("INV_CODE", invCode);
        List<Object[]> resultList = q.getResultList();
        resultList.stream().map((result) ->
        {
            InvAttribute attribute = new InvAttribute();
            attribute.setInvCode(String.valueOf(result[0]));
            attribute.setAttributeId(String.valueOf(result[1]));
            attribute.setAttributeName(String.valueOf(result[2]));
            attribute.setAttributeType(String.valueOf(result[3]));
            attribute.setAttributeValue(String.valueOf(result[4]));
            attribute.setWhsCode(String.valueOf(result[5]));
            attribute.setDefined(Integer.parseInt(String.valueOf(result[6])) == 1);
            return attribute;
        }).forEachOrdered(attributeList::add);

        em.close();

        return attributeList;
    }

    @Transactional
    public void updateInvAttributes(List<InvAttribute> attributeList) {
        for (InvAttribute attribute : attributeList) {
            if (attribute.isDefined()) {
                Query q;
                if (attribute.getAttributeValue().isEmpty() ||
                        (attribute.getAttributeValue().equals("0")
                                && attribute.getAttributeType().equals("BIT"))) {
                    q = em.createNativeQuery("""
                            DELETE FROM INV_ATTRIB
                            WHERE INV_CODE = :INV_CODE AND INV_ATTRIB_ID = :INV_ATTRIB_ID
                                    AND (WHS_CODE = :WHS_CODE OR INV_ATTRIB_ID NOT IN ('AT010','AT011'))""");
                    q.setParameter("INV_CODE", attribute.getInvCode());
                    q.setParameter("INV_ATTRIB_ID", attribute.getAttributeId());
                    q.setParameter("WHS_CODE", attribute.getWhsCode());
                } else {
                    q = em.createNativeQuery("""
                            UPDATE INV_ATTRIB
                            SET ATTRIB_VALUE = :ATTRIB_VALUE
                            WHERE INV_CODE = :INV_CODE AND INV_ATTRIB_ID = :INV_ATTRIB_ID
                                    AND (WHS_CODE = :WHS_CODE OR INV_ATTRIB_ID NOT IN ('AT010','AT011'))""");
                    q.setParameter("ATTRIB_VALUE", attribute.getAttributeValue());
                    q.setParameter("INV_CODE", attribute.getInvCode());
                    q.setParameter("INV_ATTRIB_ID", attribute.getAttributeId());
                    q.setParameter("WHS_CODE", attribute.getWhsCode());
                }
                q.executeUpdate();
            } else if (!attribute.getAttributeValue().isEmpty()) {
                Query q = em.createNativeQuery("""
                        INSERT INTO INV_ATTRIB(
                            INV_CODE,
                            INV_ATTRIB_ID,
                            ATTRIB_VALUE,
                            WHS_CODE)
                        VALUES (
                            :INV_CODE,
                            :INV_ATTRIB_ID,
                            :ATTRIB_VALUE,
                            :WHS_CODE)""");
                q.setParameter("INV_CODE", attribute.getInvCode());
                q.setParameter("INV_ATTRIB_ID", attribute.getAttributeId());
                q.setParameter("ATTRIB_VALUE", attribute.getAttributeValue());
                q.setParameter("WHS_CODE", attribute.getWhsCode());
                q.executeUpdate();
            }
        }
        em.close();
    }

    @Transactional
    public boolean updateShelfBarcode(String whsCode,
                                      String shelfBarcode,
                                      String invBarcode) {
        shelfBarcode = shelfBarcode.replace("%23", "#");
        Query selectQuery = em.createNativeQuery("""
                SELECT INV_CODE FROM INV_ATTRIB
                WHERE INV_CODE = (SELECT INV_CODE FROM INV_BARCODE
                                       WHERE BAR_CODE = :BAR_CODE)
                AND INV_ATTRIB_ID = 'AT010' AND WHS_CODE = :WHS_CODE""");
        selectQuery.setParameter("BAR_CODE", invBarcode);
        selectQuery.setParameter("WHS_CODE", whsCode);
        String invCode = "";
        List<Object> resultList = selectQuery.getResultList();
        if (!resultList.isEmpty())
            invCode = String.valueOf(resultList.get(0));

        if (invCode != null && !invCode.isEmpty()) {
            Query updateQuery = em.createNativeQuery("""
                    UPDATE INV_ATTRIB
                    SET ATTRIB_VALUE = :ATTRIB_VALUE
                    WHERE INV_CODE = :INV_CODE
                        AND INV_ATTRIB_ID = 'AT010'
                        AND WHS_CODE = :WHS_CODE""");
            updateQuery.setParameter("ATTRIB_VALUE", shelfBarcode);
            updateQuery.setParameter("INV_CODE", invCode);
            updateQuery.setParameter("WHS_CODE", whsCode);
            updateQuery.executeUpdate();
        } else {
            selectQuery = em.createNativeQuery("SELECT INV_CODE from INV_BARCODE WHERE BAR_CODE = :BAR_CODE");
            selectQuery.setParameter("BAR_CODE", invBarcode);
            resultList = selectQuery.getResultList();
            if (!resultList.isEmpty()) {
                invCode = String.valueOf(resultList.get(0));
                Query insertQuery = em.createNativeQuery("""
                        INSERT INTO INV_ATTRIB(
                            INV_CODE,
                            INV_ATTRIB_ID,
                            ATTRIB_VALUE,
                            WHS_CODE)
                        VALUES (
                            :INV_CODE,
                            :INV_ATTRIB_ID,
                            :ATTRIB_VALUE,
                            :WHS_CODE)""");
                insertQuery.setParameter("INV_CODE", invCode);
                insertQuery.setParameter("INV_ATTRIB_ID", "AT010");
                insertQuery.setParameter("ATTRIB_VALUE", shelfBarcode);
                insertQuery.setParameter("WHS_CODE", whsCode);
                insertQuery.executeUpdate();
            } else {
                return false;
            }
        }

        em.close();

        return true;
    }

    public List<InvBarcode> getBarcodeList(String invCode) {
        List<InvBarcode> barcodeList = new ArrayList<>();

        Query q = em.createNativeQuery("""
                SELECT IM.INV_CODE,
                    IB.BAR_CODE,
                    IB.UOM_FACTOR,
                    IB.UOM
                FROM INV_MASTER IM
                JOIN INV_BARCODE IB ON IM.INV_CODE = IB.INV_CODE
                WHERE IM.INV_CODE = :INV_CODE""");
        q.setParameter("INV_CODE", invCode);
        List<Object[]> resultList = q.getResultList();
        resultList.stream().map((result) ->
        {
            InvBarcode barcode = new InvBarcode();
            barcode.setInvCode(String.valueOf(result[0]));
            barcode.setBarcode(String.valueOf(result[1]));
            barcode.setUomFactor(Double.parseDouble(String.valueOf(result[2])));
            barcode.setUom(String.valueOf(result[3]));
            barcode.setDefined(true);
            return barcode;
        }).forEachOrdered(barcodeList::add);

        em.close();

        return barcodeList;
    }

    @Transactional
    public void updateInvBarcodes(List<InvBarcode> barcodeList) {
        for (InvBarcode barcode : barcodeList) {
            Query q;
            if (barcode.isDefined()) {
                if (barcode.getUomFactor() == 0) {
                    q = em.createStoredProcedureQuery("SP_DELETE_BARCODE");
                    ((StoredProcedureQuery) q).registerStoredProcedureParameter("INV_CODE", String.class, IN);
                    ((StoredProcedureQuery) q).registerStoredProcedureParameter("BAR_CODE", String.class, IN);
                } else {
                    q = em.createNativeQuery("""
                            UPDATE INV_BARCODE
                            SET UOM_FACTOR = :UOM_FACTOR,
                                UOM = :UOM
                            WHERE INV_CODE = :INV_CODE AND BAR_CODE = :BAR_CODE""");
                    q.setParameter("UOM_FACTOR", barcode.getUomFactor());
                    q.setParameter("UOM", barcode.getUom());
                }
                q.setParameter("INV_CODE", barcode.getInvCode());
                q.setParameter("BAR_CODE", barcode.getBarcode());
            } else {
                q = em.createStoredProcedureQuery("SP_INSERT_BARCODE");
                ((StoredProcedureQuery) q).registerStoredProcedureParameter("INV_CODE", String.class, IN);
                ((StoredProcedureQuery) q).registerStoredProcedureParameter("BAR_CODE", String.class, IN);
                ((StoredProcedureQuery) q).registerStoredProcedureParameter("UOM", String.class, IN);
                ((StoredProcedureQuery) q).registerStoredProcedureParameter("UOM_FACTOR", Double.class, IN);
                q.setParameter("INV_CODE", barcode.getInvCode());
                q.setParameter("BAR_CODE", barcode.getBarcode());
                q.setParameter("UOM_FACTOR", barcode.getUomFactor());
                q.setParameter("UOM", barcode.getUom());
            }
            q.executeUpdate();
        }
        em.close();
    }

    public List<Inventory> getInvList() {
        List<Inventory> inventoryList = new ArrayList<>();

        Query q = em.createNativeQuery("""
                SELECT IM.INV_CODE,
                    IM.INV_NAME,
                    IM.INV_BRAND_CODE,
                    IB.BAR_CODE,
                    PL.PRICE,ISNULL(IA.ATTRIB_VALUE, '')
                FROM INV_MASTER IM
                JOIN INV_BARCODE IB ON IM.INV_CODE=IB.INV_CODE
                JOIN INV_ATTRIB IA ON IM.INV_CODE = IA.INV_CODE AND IA.INV_ATTRIB_ID='AT019'
                JOIN PRICE_LIST PL ON IM.INV_CODE = PL.INV_CODE AND PL.PRICE_CODE = 'P01'
                ORDER BY IM.INV_CODE""");
        List<Object[]> resultList = q.getResultList();
        resultList.stream().map((result) ->
        {
            Inventory inventory = new Inventory();
            inventory.setInvCode(String.valueOf(result[0]));
            inventory.setInvName(String.valueOf(result[1]));
            inventory.setInvBrand(String.valueOf(result[2]));
            inventory.setBarcode(String.valueOf(result[3]));
            inventory.setPrice(Double.parseDouble(String.valueOf(result[4])));
            inventory.setInternalCount(String.valueOf(result[5]));
            return inventory;
        }).forEachOrdered(inventoryList::add);

        em.close();

        return inventoryList;
    }

    public List<Inventory> getInvListByUser(String userId) {
        List<Inventory> inventoryList = new ArrayList<>();

        StoredProcedureQuery q = em.createStoredProcedureQuery("SP_GET_INV_LIST_BY_USER_PRODUCER");
        q.registerStoredProcedureParameter("USER_ID", String.class, IN);
        q.setParameter("USER_ID", userId);
        List<Object[]> resultList = q.getResultList();
        for (Object[] result : resultList) {
            Inventory inventory = new Inventory();
            inventory.setInvCode(String.valueOf(result[0]));
            inventory.setInvName(String.valueOf(result[1]));
            inventory.setInvBrand(String.valueOf(result[2]));
            inventory.setBarcode(String.valueOf(result[3]));
            inventory.setInternalCount(String.valueOf(result[4]));
            inventoryList.add(inventory);
        }

        em.close();

        return inventoryList;
    }

    public List<Inventory> getWhsSumByUser(String userId, String whsCode) {
        List<Inventory> inventoryList = new ArrayList<>();

        StoredProcedureQuery q = em.createStoredProcedureQuery("SP_GET_WHS_SUM_FOR_TERMINAL_INT_USE");
        q.registerStoredProcedureParameter("USER_ID", String.class, IN);
        q.registerStoredProcedureParameter("WHS_CODE", String.class, IN);
        q.setParameter("USER_ID", userId);
        q.setParameter("WHS_CODE", whsCode);
        List<Object[]> resultList = q.getResultList();
        for (Object[] result : resultList) {
            Inventory inventory = new Inventory();
            inventory.setInvCode(String.valueOf(result[0]));
            inventory.setInvName(String.valueOf(result[1]));
            inventory.setInvBrand(String.valueOf(result[2]));
            inventory.setBarcode(String.valueOf(result[3]));
            inventory.setInternalCount(String.valueOf(result[4]));
            inventory.setWhsQty(Double.parseDouble(String.valueOf(result[5])));
            inventory.setPrice(Double.parseDouble(String.valueOf(result[6])));
            inventoryList.add(inventory);
        }

        em.close();

        return inventoryList;
    }

    public InvBarcode getInvBarcode(String barcode) {
        InvBarcode invBarcode = new InvBarcode();

        Query q = em.createNativeQuery("""
                SELECT IB.INV_CODE,
                    IB.BAR_CODE,
                    IB.UOM_FACTOR
                FROM INV_BARCODE IB
                WHERE BAR_CODE = :BAR_CODE""");
        q.setParameter("BAR_CODE", barcode);
        List<Object[]> resultList = q.getResultList();
        if (!resultList.isEmpty()) {
            invBarcode.setInvCode(String.valueOf(resultList.get(0)[0]));
            invBarcode.setBarcode(String.valueOf(resultList.get(0)[1]));
            invBarcode.setUomFactor(Double.parseDouble(String.valueOf(resultList.get(0)[2])));
            invBarcode.setDefined(true);
        }

        em.close();

        return invBarcode;
    }

    public List<LatestMovementItem> getLatestMovementItems(String invCode, String whsCode, int top) {
        List<LatestMovementItem> latestMovementItemList = new ArrayList<>();
        String queryString = String.format("""
                        SELECT TOP %s *
                        FROM
                        (
                            SELECT TRX_NO,
                                   TRX_DATE,
                                   DBT_CRD * QTY * UOM_FACTOR AS QTY
                            FROM INV_TRX
                            WHERE INV_CODE = :INV_CODE
                                  AND WHS_CODE = :WHS_CODE
                            UNION ALL
                            SELECT TRX_NO,
                                   TRX_DATE,
                                   DBT_CRD * QTY * UOM_FACTOR AS QTY
                            FROM IVC_TRX
                            WHERE((TRX_TYPE_ID = 17
                                   AND (PREV_TRX_TYPE_ID != 25
                                        OR PREV_TRX_TYPE_ID IS NULL))
                                  OR TRX_TYPE_ID != 17)
                                 AND INV_CODE = :INV_CODE
                                 AND WHS_CODE = :WHS_CODE
                        ) T
                        ORDER BY TRX_DATE DESC""",
                top);
        Query query = em.createNativeQuery(queryString);

        query.setParameter("WHS_CODE", whsCode);
        query.setParameter("INV_CODE", invCode);

        List<Object[]> resultList = query.getResultList();
        for (Object[] result : resultList) {
            LatestMovementItem latestMovementItem = new LatestMovementItem();
            latestMovementItem.setTrxNo(String.valueOf(result[0]));
            latestMovementItem.setTrxDate(String.valueOf(result[1]));
            latestMovementItem.setQuantity(Double.parseDouble(String.valueOf(result[2])));
            latestMovementItemList.add(latestMovementItem);
        }
        em.close();
        return latestMovementItemList;
    }

    public List<Brand> getBrandList() {
        List<Brand> brandList = new ArrayList<>();
        Query query = em.createNativeQuery("""
                SELECT INV_BRAND_CODE, INV_BRAND_NAME FROM INV_BRAND;
                """);
        List<Object[]> resultList = query.getResultList();
        for (Object[] result : resultList) {
            Brand brand = new Brand();
            brand.setBrandCode(String.valueOf(result[0]));
            brand.setBrandName(String.valueOf(result[1]));
            brandList.add(brand);
        }
        em.close();
        return brandList;
    }

    @Transactional
    public void updateInvMasterData(UpdateInvRequest request) {
        Query query = em.createNativeQuery("""
                UPDATE INV_MASTER
                SET INV_BRAND_CODE = :INV_BRAND_CODE,
                    LAST_REC_USER = :LAST_REC_USER,
                    LAST_HOST_NAME = :LAST_HOST_NAME,
                    LAST_REC_DATE = CONVERT(DATETIME2(0), GETDATE())
                WHERE INV_CODE = :INV_CODE;
                """);
        query.setParameter("INV_BRAND_CODE", request.getBrandCode());
        query.setParameter("INV_CODE", request.getInvCode());
        query.setParameter("LAST_REC_USER", request.getUserId());
        query.setParameter("LAST_HOST_NAME", request.getDeviceId());
        query.executeUpdate();
        em.close();
    }
}
