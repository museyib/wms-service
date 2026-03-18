package az.inci.wms.services.v4;

import az.inci.wms.model.v4.*;
import az.inci.wms.services.AbstractService;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.ParameterMode.IN;

@Service
public class LogisticsServiceV4 extends AbstractService {

    private final UserServiceV4 userService;

    public LogisticsServiceV4(UserServiceV4 userService) {
        this.userService = userService;
    }


    public ShipDocInfo getDocInfoByTrxNo(String trxNo) {
        ShipDocInfo shipDocInfo = null;

        Query query = em.createNativeQuery("""
                SELECT TOP 1 SD.DRIVER_CODE,
                    PM.PER_NAME,
                    SD.VEHICLE_CODE,
                    ISNULL(ST.DELIVER_NOTES, '') AS DELIVER_NOTES,
                    ST.SHIP_STATUS,
                    SS.SHIP_STATUS_DESC
                FROM SHIP_TRX ST
                JOIN SHIP_DOC SD ON ST.TRX_NO = SD.TRX_NO
                JOIN PER_MASTER PM ON SD.DRIVER_CODE = PM.PER_CODE
                JOIN SHIP_STATUS SS ON ST.SHIP_STATUS = SS.SHIP_STATUS_CODE
                WHERE ST.SRC_TRX_NO = :SRC_TRX_NO
                ORDER BY ST.TRX_ID DESC""");

        query.setParameter("SRC_TRX_NO", trxNo);

        List<Object[]> resultList = query.getResultList();

        if (!resultList.isEmpty()) {
            shipDocInfo = new ShipDocInfo();
            Object[] result = resultList.get(0);
            shipDocInfo.setDriverCode((String) result[0]);
            shipDocInfo.setDriverName((String) result[1]);
            shipDocInfo.setVehicleCode((String) result[2]);
            shipDocInfo.setDeliverNotes((String) result[3]);
            shipDocInfo.setShipStatus((String) result[4]);
            shipDocInfo.setShipStatusDescription((String) result[5]);
        }

        em.close();

        return shipDocInfo;
    }

    public ShipDocInfo getDocInfoForConfirmByTrxNo(String trxNo) {
        ShipDocInfo shipDocInfo = null;

        Query query = em.createNativeQuery("""
                SELECT TOP 1 SD.DRIVER_CODE,
                    PM.PER_NAME,
                    SD.VEHICLE_CODE,
                    ISNULL(ST.DELIVER_NOTES, '') AS DELIVER_NOTES,
                    ST.SHIP_STATUS,
                    ST.DELIVER_FLAG
                FROM SHIP_TRX ST
                JOIN SHIP_DOC SD ON ST.TRX_NO = SD.TRX_NO
                JOIN PER_MASTER PM ON SD.DRIVER_CODE = PM.PER_CODE
                WHERE ST.SHIP_STATUS IN ('AC', 'YC', 'MG')
                        AND ST.SRC_TRX_NO = :SRC_TRX_NO
                ORDER BY ST.TRX_ID DESC""");

        query.setParameter("SRC_TRX_NO", trxNo);

        List<Object[]> resultList = query.getResultList();

        if (!resultList.isEmpty()) {
            shipDocInfo = new ShipDocInfo();
            Object[] result = resultList.get(0);
            shipDocInfo.setDriverCode((String) result[0]);
            shipDocInfo.setDriverName((String) result[1]);
            shipDocInfo.setVehicleCode((String) result[2]);
            shipDocInfo.setDeliverNotes((String) result[3]);
            shipDocInfo.setShipStatus((String) result[4]);
            shipDocInfo.setDeliverFlag(Boolean.parseBoolean(String.valueOf(result[5])));
        }

        em.close();

        return shipDocInfo;
    }

    public ShipDocInfo getDocInfoForArchiveByTrxNo(String trxNo) {
        ShipDocInfo shipDocInfo = null;

        Query query = em.createNativeQuery("""
                SELECT TOP 1 SD.DRIVER_CODE,
                    PM.PER_NAME,
                    SD.VEHICLE_CODE,
                    ISNULL(ST.DELIVER_NOTES, '') AS DELIVER_NOTES,
                    ST.SHIP_STATUS,
                    ST.CONFIRM_FLAG,
                    ST.DELIVER_FLAG,
                    ST.ARCHIVE_FLAG
                FROM SHIP_TRX ST
                JOIN SHIP_DOC SD ON ST.TRX_NO = SD.TRX_NO
                JOIN PER_MASTER PM ON SD.DRIVER_CODE = PM.PER_CODE
                WHERE ST.SRC_TRX_NO = :SRC_TRX_NO
                ORDER BY ST.TRX_ID DESC""");

        query.setParameter("SRC_TRX_NO", trxNo);

        List<Object[]> resultList = query.getResultList();

        if (!resultList.isEmpty()) {
            shipDocInfo = new ShipDocInfo();
            Object[] result = resultList.get(0);
            shipDocInfo.setDriverCode((String) result[0]);
            shipDocInfo.setDriverName((String) result[1]);
            shipDocInfo.setVehicleCode((String) result[2]);
            shipDocInfo.setDeliverNotes((String) result[3]);
            shipDocInfo.setShipStatus((String) result[4]);
            shipDocInfo.setConfirmFlag(Boolean.parseBoolean(String.valueOf(result[5])));
            shipDocInfo.setDeliverFlag(Boolean.parseBoolean(String.valueOf(result[6])));
            shipDocInfo.setArchiveFlag(Boolean.parseBoolean(String.valueOf(result[7])));
        }

        em.close();

        return shipDocInfo;
    }

    public ShipDocInfo getDocInfoForSendingByTrxNo(String trxNo) {
        ShipDocInfo shipDocInfo = null;

        Query query = em.createNativeQuery("""
                SELECT TOP 1 SD.DRIVER_CODE,
                    PM.PER_NAME,
                    SD.VEHICLE_CODE,
                    ISNULL(ST.DELIVER_NOTES, '') AS DELIVER_NOTES,
                    ST.SHIP_STATUS
                FROM SHIP_TRX ST
                JOIN SHIP_DOC SD ON ST.TRX_NO = SD.TRX_NO
                JOIN PER_MASTER PM ON SD.DRIVER_CODE = PM.PER_CODE
                WHERE ST.SHIP_STATUS IN ('AC','PL','MG')
                        AND ST.SRC_TRX_NO = :SRC_TRX_NO
                ORDER BY ST.TRX_ID DESC""");

        query.setParameter("SRC_TRX_NO", trxNo);

        List<Object[]> resultList = query.getResultList();

        if (!resultList.isEmpty()) {
            shipDocInfo = new ShipDocInfo();
            Object[] result = resultList.get(0);
            shipDocInfo.setDriverCode((String) result[0]);
            shipDocInfo.setDriverName((String) result[1]);
            shipDocInfo.setVehicleCode((String) result[2]);
            shipDocInfo.setDeliverNotes((String) result[3]);
            shipDocInfo.setShipStatus((String) result[4]);
        }

        em.close();

        return shipDocInfo;
    }

    public ShipDocInfo getDocInfoForReturnByTrxNo(String trxNo) {
        ShipDocInfo shipDocInfo = null;

        Query query = em.createNativeQuery("""
                SELECT TOP 1 SD.DRIVER_CODE,
                PM.PER_NAME,
                SD.VEHICLE_CODE,
                ISNULL(ST.DELIVER_NOTES, '') AS DELIVER_NOTES,
                ST.SHIP_STATUS
                FROM SHIP_TRX ST
                JOIN SHIP_DOC SD ON ST.TRX_NO = SD.TRX_NO
                JOIN PER_MASTER PM ON SD.DRIVER_CODE = PM.PER_CODE
                WHERE ST.SHIP_STATUS IN ('YC', 'AC', 'PL', 'MG')
                        AND ST.SRC_TRX_NO = :SRC_TRX_NO
                ORDER BY ST.TRX_ID DESC""");

        query.setParameter("SRC_TRX_NO", trxNo);

        List<Object[]> resultList = query.getResultList();

        if (!resultList.isEmpty()) {
            shipDocInfo = new ShipDocInfo();
            Object[] result = resultList.get(0);
            shipDocInfo.setDriverCode((String) result[0]);
            shipDocInfo.setDriverName((String) result[1]);
            shipDocInfo.setVehicleCode((String) result[2]);
            shipDocInfo.setDeliverNotes((String) result[3]);
            shipDocInfo.setShipStatus((String) result[4]);
        }

        em.close();

        return shipDocInfo;
    }

    public ShipDocInfo getDocInfoForDeliveryByTrxNo(String trxNo) {
        ShipDocInfo shipDocInfo = null;

        Query query = em.createNativeQuery("""
                SELECT TOP 1 SD.DRIVER_CODE,
                    PM.PER_NAME,
                    SD.VEHICLE_CODE,
                    ISNULL(ST.DELIVER_NOTES, '') AS DELIVER_NOTES,
                    ST.SHIP_STATUS,
                    ISNULL(AD.BP_CODE, ISNULL(ID.TRG_WHS_CODE, ISNULL(OT.EXP_CENTER_CODE, ET.EXP_CENTER_CODE))) AS TRG_CODE,
                    ISNULL(AD.BP_NAME, ISNULL(WM.WHS_NAME, EC.EXP_CENTER_DESC)) AS TRG_NAME,
                    ISNULL(BMC.LONGITUDE, 0) AS LONGITUDE,
                    ISNULL(BMC.LATITUDE, 0) AS LATITUDE,
                    ST.TRX_ID
                FROM SHIP_TRX ST
                JOIN SHIP_DOC SD ON ST.TRX_NO = SD.TRX_NO
                JOIN PER_MASTER PM ON SD.DRIVER_CODE = PM.PER_CODE
                LEFT JOIN INV_DOC ID ON ST.SRC_TRX_NO = ID.TRX_NO
                LEFT JOIN ACC_DOC AD ON ST.SRC_TRX_NO = AD.TRX_NO
                LEFT JOIN OEXP_TRX OT ON ST.SRC_TRX_NO = OT.TRX_NO
                LEFT JOIN EXP_TRX ET ON ST.SRC_TRX_NO = ET.TRX_NO
                LEFT JOIN DELIVER_POINT_COORDINATE BMC ON ISNULL(AD.BP_CODE, ID.TRG_WHS_CODE) = BMC.TARGET_CODE
                LEFT JOIN WHS_MASTER WM ON ID.TRG_WHS_CODE = WM.WHS_CODE
                LEFT JOIN EXP_CENTER EC ON OT.EXP_CENTER_CODE = EC.EXP_CENTER_CODE OR ET.EXP_CENTER_CODE = EC.EXP_CENTER_CODE
                WHERE ST.SHIP_STATUS IN ('AC','YC','MG') AND ST.SRC_TRX_NO = :SRC_TRX_NO
                ORDER BY ST.TRX_ID DESC""");

        query.setParameter("SRC_TRX_NO", trxNo);

        List<Object[]> resultList = query.getResultList();

        if (!resultList.isEmpty()) {
            shipDocInfo = new ShipDocInfo();
            Object[] result = resultList.get(0);
            shipDocInfo.setDriverCode((String) result[0]);
            shipDocInfo.setDriverName((String) result[1]);
            shipDocInfo.setVehicleCode((String) result[2]);
            shipDocInfo.setDeliverNotes((String) result[3]);
            shipDocInfo.setShipStatus((String) result[4]);
            shipDocInfo.setTargetCode((String) result[5]);
            shipDocInfo.setTargetName((String) result[6]);
            shipDocInfo.setLongitude(Double.parseDouble((String) result[7]));
            shipDocInfo.setLatitude(Double.parseDouble((String) result[8]));
            shipDocInfo.setTrxId(String.valueOf(result[9]));
        }

        em.close();

        return shipDocInfo;
    }

    public List<ShipDoc> getDocList(String startDate, String endDate) {
        List<ShipDoc> docList = new ArrayList<>();

        Query query = em.createNativeQuery("""
                SELECT ST.TRX_NO,
                    ST.SRC_TRX_NO,
                    dbo.fnFormatDate(ST.TRX_DATE, 'yyyy-MM-dd'),
                    ISNULL(AD.BP_CODE, ISNULL(ID.TRG_WHS_CODE,  ISNULL(OT.EXP_CENTER_CODE, ET.EXP_CENTER_CODE))) AS CUST_CODE,
                    ISNULL(AD.BP_NAME, ISNULL(WM.WHS_NAME, EC.EXP_CENTER_DESC)) AS CUST_NAME,
                    ISNULL(AD.SBE_CODE, '') AS SBE_CODE,
                    ISNULL(AD.SBE_NAME, '') AS SBE_NAME,
                    ISNULL(ST.DELIVER_NOTES, '') AS DELIVER_NOTES,
                    PM.PER_NAME,
                    ST.SHIP_STATUS
                FROM SHIP_TRX ST
                LEFT JOIN SHIP_DOC SD ON ST.TRX_NO = SD.TRX_NO
                LEFT JOIN INV_DOC ID ON ST.SRC_TRX_NO = ID.TRX_NO
                LEFT JOIN ACC_DOC AD ON ST.SRC_TRX_NO = AD.TRX_NO
                LEFT JOIN OEXP_TRX OT ON ST.SRC_TRX_NO = OT.TRX_NO
                LEFT JOIN EXP_TRX ET ON ST.SRC_TRX_NO = ET.TRX_NO
                LEFT JOIN EXP_CENTER EC ON OT.EXP_CENTER_CODE = EC.EXP_CENTER_CODE OR ET.EXP_CENTER_CODE = EC.EXP_CENTER_CODE
                LEFT JOIN WHS_MASTER WM ON ID.TRG_WHS_CODE = WM.WHS_CODE
                LEFT JOIN PER_MASTER PM ON SD.DRIVER_CODE = PM.PER_CODE
                WHERE ST.TRX_DATE BETWEEN :START_DATE AND :END_DATE
                ORDER BY ST.TRX_NO DESC""");

        query.setParameter("START_DATE", startDate);
        query.setParameter("END_DATE", endDate);

        List<Object[]> resultList = query.getResultList();

        resultList.stream().map((result) ->
        {
            ShipDoc doc = new ShipDoc();
            doc.setDocNo((String) result[0]);
            doc.setTrxNo((String) result[1]);
            doc.setTrxDate((String) result[2]);
            doc.setBpCode((String) result[3]);
            doc.setBpName((String) result[4]);
            doc.setSbeCode((String) result[5]);
            doc.setSbeName((String) result[6]);
            doc.setDriverCode((String) result[7]);
            doc.setDriverName((String) result[8]);
            doc.setShipStatus((String) result[9]);
            return doc;
        }).forEachOrdered(docList::add);

        em.close();

        return docList;
    }

    @Transactional
    public void changeDocStatus(UpdateDeliveryRequest request) {

        for (UpdateDeliveryRequestItem requestItem : request.getRequestItems()) {
            StoredProcedureQuery query = em.createStoredProcedureQuery("SP_CHANGE_SHIP_STATUS");
            query.registerStoredProcedureParameter("TRX_NO", String.class, IN);
            query.registerStoredProcedureParameter("STATUS", String.class, IN);
            query.registerStoredProcedureParameter("DRIVER_CODE", String.class, IN);
            query.registerStoredProcedureParameter("NOTE", String.class, IN);
            query.registerStoredProcedureParameter("DELIVER_PERSON", String.class, IN);
            query.setParameter("TRX_NO", requestItem.getTrxNo());
            query.setParameter("STATUS", request.getStatus());
            query.setParameter("DRIVER_CODE", requestItem.getDriverCode());
            query.setParameter("NOTE", requestItem.getNote());
            query.setParameter("DELIVER_PERSON", requestItem.getDeliverPerson());

            query.executeUpdate();
        }

        em.close();
    }

    @Transactional
    public void confirmDelivery(ConfirmDeliveryRequest request) {
        StoredProcedureQuery query = em.createStoredProcedureQuery("SP_CONFIRM_DELIVERY");
        query.registerStoredProcedureParameter("TRX_NO", String.class, IN);
        query.registerStoredProcedureParameter("DRIVER_CODE", String.class, IN);
        query.registerStoredProcedureParameter("NOTE", String.class, IN);
        query.registerStoredProcedureParameter("DELIVER_PERSON", String.class, IN);
        query.registerStoredProcedureParameter("TRANSITION_FLAG", Boolean.class, IN);
        query.setParameter("TRX_NO", request.getTrxNo());
        query.setParameter("DRIVER_CODE", request.getDriverCode());
        query.setParameter("NOTE", request.getNote());
        query.setParameter("DELIVER_PERSON", request.getDeliverPerson());
        query.setParameter("TRANSITION_FLAG", request.isTransitionFlag());

        query.executeUpdate();

        em.close();
    }

    @Transactional
    public void confirmShipment(List<UpdateDeliveryRequestItem> requestList) {

        UpdateDeliveryRequestItem requestItem = requestList.get(0);
        String userId = requestItem.getNote().replace("İstifadəçi: ", "");
        User user = userService.getById(userId);
        if (!user.isApplyShipCentralFlag() && requestItem.isTransitionFlag()) {
            throw new IllegalStateException("Mərkəzə qəbul üçün səlahiyyətiniz yoxdur!");
        }
        for (UpdateDeliveryRequestItem request : requestList) {
            StoredProcedureQuery query = em.createStoredProcedureQuery("SP_CONFIRM_SHIPMENT");
            query.registerStoredProcedureParameter("TRX_NO", String.class, IN);
            query.registerStoredProcedureParameter("DRIVER_CODE", String.class, IN);
            query.registerStoredProcedureParameter("NOTE", String.class, IN);
            query.registerStoredProcedureParameter("DELIVER_PERSON", String.class, IN);
            query.registerStoredProcedureParameter("TRANSITION_FLAG", Boolean.class, IN);
            query.setParameter("TRX_NO", request.getTrxNo());
            query.setParameter("DRIVER_CODE", request.getDriverCode());
            query.setParameter("NOTE", request.getNote());
            query.setParameter("DELIVER_PERSON", request.getDeliverPerson());
            query.setParameter("TRANSITION_FLAG", request.isTransitionFlag());

            query.executeUpdate();
        }

        em.close();
    }

    @Transactional
    public void archiveDelivery(List<ArchiveDeliveryRequest> requestList) {
        for (ArchiveDeliveryRequest request : requestList) {
            StoredProcedureQuery query = em.createStoredProcedureQuery("SP_ARCHIVE_DELIVERY");
            query.registerStoredProcedureParameter("TRX_NO", String.class, IN);
            query.registerStoredProcedureParameter("DRIVER_CODE", String.class, IN);
            query.setParameter("TRX_NO", request.getTrxNo());
            query.setParameter("DRIVER_CODE", request.getDriverCode());

            query.executeUpdate();
        }

        em.close();
    }

    @Transactional
    public void updateDocLocation(UpdateDocLocationRequest request) {
        Query query = em.createNativeQuery("""
                UPDATE SHIP_TRX
                SET LATITUDE = :LATITUDE,
                    LONGITUDE = :LONGITUDE,
                    ADDRESS = :ADDRESS
                FROM SHIP_DOC SD
                JOIN SHIP_TRX ST ON SD.TRX_NO = ST.TRX_NO
                WHERE ST.SHIP_STATUS = 'YC'
                    AND SD.DRIVER_CODE = (SELECT PER_CODE
                                            FROM PER_MASTER
                                            WHERE USER_ID = :USER_ID)""");
        query.setParameter("LATITUDE", request.getLatitude());
        query.setParameter("LONGITUDE", request.getLongitude());
        query.setParameter("ADDRESS", request.getAddress());
        query.setParameter("USER_ID", request.getUserId());
        query.executeUpdate();

        em.close();
    }

    public List<ShipDoc> getNotConfirmedDocList(String startDate, String endDate, String driverCode) {
        List<ShipDoc> docList = new ArrayList<>();

        Query query = em.createNativeQuery("""
                SELECT ST.TRX_NO,
                    ST.SRC_TRX_NO,
                    dbo.fnFormatDate(ST.TRX_DATE, 'yyyy-MM-dd'),
                    ISNULL(AD.BP_CODE, ISNULL(ID.TRG_WHS_CODE, ET.EXP_CENTER_CODE)) AS CUST_CODE,
                    ISNULL(AD.BP_NAME, ISNULL(WM.WHS_NAME, EC.EXP_CENTER_DESC)) AS CUST_NAME,
                    ISNULL(AD.SBE_CODE, '') AS SBE_CODE,
                    ISNULL(AD.SBE_NAME, '') AS SBE_NAME,
                    ISNULL(ST.DELIVER_NOTES, '') AS DELIVER_NOTES,
                    PM.PER_NAME,
                    ST.SHIP_STATUS
                FROM SHIP_TRX ST
                LEFT JOIN SHIP_DOC SD ON ST.TRX_NO = SD.TRX_NO
                LEFT JOIN INV_DOC ID ON ST.SRC_TRX_NO = ID.TRX_NO
                LEFT JOIN ACC_DOC AD ON ST.SRC_TRX_NO = AD.TRX_NO
                LEFT JOIN EXP_TRX ET ON ST.SRC_TRX_NO = ET.TRX_NO
                LEFT JOIN EXP_CENTER EC ON ET.EXP_CENTER_CODE = EC.EXP_CENTER_CODE
                LEFT JOIN WHS_MASTER WM ON ID.TRG_WHS_CODE = WM.WHS_CODE
                LEFT JOIN PER_MASTER PM ON SD.DRIVER_CODE = PM.PER_CODE
                WHERE ST.TRX_DATE BETWEEN :START_DATE AND :END_DATE
                        AND SD.DRIVER_CODE LIKE :DRIVER_CODE
                        AND ST.SHIP_STATUS NOT IN('MC', 'MD')
                ORDER BY ST.TRX_NO DESC""");

        query.setParameter("START_DATE", startDate);
        query.setParameter("END_DATE", endDate);
        query.setParameter("DRIVER_CODE", "%" + driverCode + "%");

        List<Object[]> resultList = query.getResultList();

        resultList.stream().map((result) ->
        {
            ShipDoc doc = new ShipDoc();
            doc.setDocNo((String) result[0]);
            doc.setTrxNo((String) result[1]);
            doc.setTrxDate((String) result[2]);
            doc.setBpCode((String) result[3]);
            doc.setBpName((String) result[4]);
            doc.setSbeCode((String) result[5]);
            doc.setSbeName((String) result[6]);
            doc.setDriverCode((String) result[7]);
            doc.setDriverName((String) result[8]);
            doc.setShipStatus((String) result[9]);
            return doc;
        }).forEachOrdered(docList::add);

        em.close();

        return docList;
    }

    public boolean checkDeliveryConfirmationCode(String trxNo, String confirmationCode) {
        boolean isValid = false;

        Query query = em.createNativeQuery("""
                SELECT dbo.FN_CHECK_DELIVERY_CONFIRMATION_CODE(:TRX_NO, :CONFIRMATION_CODE)""");

        query.setParameter("TRX_NO", trxNo);
        query.setParameter("CONFIRMATION_CODE", confirmationCode);

        List<Boolean> resultList = query.getResultList();

        if (!resultList.isEmpty()) {
            isValid = resultList.get(0);
        }

        em.close();

        return isValid;
    }

    public List<WaitingDocToShip> getWaitingDocListToShip(String driverCode) {
        List<WaitingDocToShip> docList = new ArrayList<>();

        Query query = em.createNativeQuery("""
                SELECT AD.TRX_NO,
                    dbo.fnFormatDate(AD.TRX_DATE, 'yyyy-MM-dd') AS TRX_DATE,
                    AD.WHS_CODE,
                    AD.BP_CODE,
                    AD.BP_NAME,
                    AD.SBE_CODE,
                    AD.SBE_NAME
                FROM ACC_DOC AD
                JOIN BP_DRIVER BD
                    ON AD.BP_CODE = BD.BP_CODE
                        AND BD.DRIVER_CODE LIKE :DRIVER_CODE
                LEFT JOIN SHIP_TRX ST
                    ON AD.TRX_NO = ST.SRC_TRX_NO
                WHERE AD.TRX_TYPE_ID = 25
                    AND ST.TRX_NO IS NULL
                    AND AD.TRX_DATE >= DATEADD(DAY, -5, GETDATE())
                    AND AD.WHS_CODE IN ('01', '11', '08', 'T20', '13', '20')""");

        query.setParameter("DRIVER_CODE", "%" + driverCode + "%");

        List<Object[]> resultList = query.getResultList();

        resultList.stream().map((result) ->
        {
            WaitingDocToShip doc = new WaitingDocToShip();
            doc.setTrxNo((String) result[0]);
            doc.setTrxDate((String) result[1]);
            doc.setWhsCode((String) result[2]);
            doc.setBpCode((String) result[3]);
            doc.setBpName((String) result[4]);
            doc.setSbeCode((String) result[5]);
            doc.setSbeName((String) result[6]);
            return doc;
        }).forEachOrdered(docList::add);

        em.close();

        return docList;
    }
}
