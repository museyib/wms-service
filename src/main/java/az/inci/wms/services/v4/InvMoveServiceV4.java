package az.inci.wms.services.v4;

import az.inci.wms.exception.OperationNotCompletedException;
import az.inci.wms.model.v4.*;
import az.inci.wms.services.AbstractService;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.ParameterMode.IN;

@Service
public class InvMoveServiceV4 extends AbstractService {
    public List<PickTrx> getSplitTrxList(String bpCode, String invCode, double qty) {
        List<PickTrx> trxList = new ArrayList<>();
        Query query = em.createNativeQuery("EXEC DBO.SP_TRX_FOR_RETURN :BP_CODE, :INV_CODE, :QTY");
//        StoredProcedureQuery query = em.createStoredProcedureQuery("SP_TRX_FOR_RETURN");
//        query.registerStoredProcedureParameter("BP_CODE", String.class, IN);
//        query.registerStoredProcedureParameter("INV_CODE", String.class, IN);
//        query.registerStoredProcedureParameter("QTY", Double.class, IN);
        query.setParameter("BP_CODE", bpCode);
        query.setParameter("INV_CODE", invCode);
        query.setParameter("QTY", qty);
        List<Object[]> resultList = query.getResultList();
        resultList.stream().map((result) ->
        {
            PickTrx trx = new PickTrx();
            trx.setPrevTrxId(Integer.parseInt(String.valueOf(result[1])));
            trx.setPrevTrxNo((String) result[2]);
            trx.setTrxDate(String.valueOf(result[3]));
            trx.setQty(Double.parseDouble(String.valueOf(result[4])));
            trx.setPrice(Double.parseDouble(String.valueOf(result[5])));
            trx.setDiscountRatio(Double.parseDouble(String.valueOf(result[6])));
            return trx;
        }).forEachOrdered(trxList::add);
        em.close();

        return trxList;
    }

    @Transactional
    public void createTransfer(TransferRequest request) {
        Query query = em.createNativeQuery("""
                INSERT INTO ANDROID_WHS_TRANSFER(
                    WHS_CODE_FROM,
                    WHS_CODE_TO,
                    INV_CODE,
                    QUANTITY,
                    USER_ID)
                VALUES(
                    :WHS_CODE_FROM,
                    :WHS_CODE_TO,
                    :INV_CODE,
                    :QUANTITY,
                    :USER_ID)""");
        for (TransferRequestItem requestItem : request.getRequestItems()) {
            query.setParameter("WHS_CODE_FROM", request.getSrcWhsCode());
            query.setParameter("WHS_CODE_TO", request.getTrgWhsCode());
            query.setParameter("INV_CODE", requestItem.getInvCode());
            query.setParameter("QUANTITY", requestItem.getQty());
            query.setParameter("USER_ID", request.getUserId());
            query.executeUpdate();
        }

        StoredProcedureQuery procedureQuery = em.createStoredProcedureQuery("SP_CREATE_RINV_DOC_FROM_ANDROID_WHS_TRANSFER");
        procedureQuery.registerStoredProcedureParameter("USER_ID", String.class, IN);
        procedureQuery.setParameter("USER_ID", request.getUserId());
        procedureQuery.execute();
        em.close();
    }

    @Transactional
    public void insertProductApproveData(ProductApproveRequest request) {
        Query query = em.createNativeQuery("""
                IF NOT EXISTS(SELECT * FROM BMSB1.dbo.TERMINAL_APPROVE
                                       WHERE SYSTEM_NO=:SYSTEM_NO
                                         AND INV_CODE=:INV_CODE
                                         AND USER_ID=:USER_ID)
                INSERT INTO TERMINAL_APPROVE(
                    SYSTEM_NO,
                    SYSTEM_DATE,
                    INV_CODE,
                    INV_QTY,
                    NOTES,
                    USER_ID,
                    INV_BRAND_CODE,
                    INV_NAME,
                    BARCODE,
                    INTERNAL_COUNT,
                    STATUS)
                VALUES(
                    :SYSTEM_NO,
                    :SYSTEM_DATE,
                    :INV_CODE,
                    :INV_QTY,
                    :NOTES,
                    :USER_ID,
                    :INV_BRAND_CODE,
                    :INV_NAME,
                    :BARCODE,
                    :INTERNAL_COUNT,
                    :STATUS)""");

        for (ProductApproveRequestItem item : request.getRequestItems()) {
            query.setParameter("SYSTEM_NO", request.getTrxNo());
            query.setParameter("SYSTEM_DATE", request.getTrxDate());
            query.setParameter("INV_CODE", item.getInvCode());
            query.setParameter("INV_QTY", item.getQty());
            query.setParameter("NOTES", request.getNotes());
            query.setParameter("USER_ID", request.getUserId());
            query.setParameter("INV_BRAND_CODE", item.getInvBrand());
            query.setParameter("INV_NAME", item.getInvName());
            query.setParameter("BARCODE", item.getBarcode());
            query.setParameter("INTERNAL_COUNT", request.getNotes());
            query.setParameter("STATUS", request.getStatus());
            query.executeUpdate();
        }
        if (request.getStatus() == 2) {
            StoredProcedureQuery procedureQuery = em.createStoredProcedureQuery("SP_CREATE_MAL_QEBULU");
            procedureQuery.registerStoredProcedureParameter("USER_ID", String.class, IN);
            procedureQuery.setParameter("USER_ID", request.getUserId());
            procedureQuery.execute();
        }

        em.close();
    }

    @Transactional
    public List<PickDoc> getApproveDocList() {
        Query query = em.createNativeQuery("""
                SELECT SYSTEM_NO,
                    dbo.fnFormatDate(SYSTEM_DATE, 'dd-mm-yyyy'),
                    NOTES
                FROM TERMINAL_APPROVE
                WHERE STATUS = 0
                GROUP BY SYSTEM_NO, SYSTEM_DATE, NOTES""");
        List<PickDoc> docList = new ArrayList<>();
        List<Object[]> resultList = query.getResultList();
        resultList.stream().map((result) ->
        {
            PickDoc doc = new PickDoc();
            doc.setTrxNo(String.valueOf(result[0]));
            doc.setTrxDate(String.valueOf(result[1]));
            doc.setNotes(String.valueOf(result[2]));
            return doc;
        }).forEachOrdered(docList::add);

        em.close();

        return docList;
    }

    public List<PickTrx> getApproveTrxList() {
        Query q = em.createNativeQuery("""
                SELECT SYSTEM_NO,
                    INV_CODE,
                    INV_NAME,
                    BARCODE,
                    INV_QTY,
                    INTERNAL_COUNT,
                    INV_BRAND_CODE
                FROM TERMINAL_APPROVE
                WHERE STATUS = 0""");
        List<PickTrx> trxList = new ArrayList<>();
        List<Object[]> resultList = q.getResultList();
        resultList.stream().map((result) ->
        {
            PickTrx trx = new PickTrx();
            trx.setTrxNo(String.valueOf(result[0]));
            trx.setInvCode(String.valueOf(result[1]));
            trx.setInvName(String.valueOf(result[2]));
            trx.setBarcode(String.valueOf(result[3]));
            trx.setQty(Double.parseDouble(String.valueOf(result[4])));
            trx.setNotes(String.valueOf(result[5]));
            trx.setInvBrand(String.valueOf(result[6]));
            return trx;
        }).forEachOrdered(trxList::add);

        em.close();

        return trxList;
    }

    @Transactional
    public void createInternalUseDoc(InternalUseRequest request) {
        Query query = em.createNativeQuery("""
                INSERT INTO TERMINAL_INV_ISSUE(
                    SYSTEM_NO,
                    SYSTEM_DATE,
                    INV_CODE,
                    INV_QTY,
                    NOTES,
                    USER_ID,
                    INV_BRAND_CODE,
                    INV_NAME,
                    BARCODE,
                    INTERNAL_COUNT,
                    WHS_CODE,
                    EXP_CENTER_CODE)
                VALUES(
                    :SYSTEM_NO,
                    :SYSTEM_DATE,
                    :INV_CODE,
                    :INV_QTY,
                    :NOTES,
                    :USER_ID,
                    :INV_BRAND_CODE,
                    :INV_NAME,
                    :BARCODE,
                    :INTERNAL_COUNT,
                    :WHS_CODE,
                    :EXP_CENTER_CODE)""");

        for (InternalUseRequestItem item : request.getRequestItems()) {
            query.setParameter("SYSTEM_NO", request.getTrxNo());
            query.setParameter("SYSTEM_DATE", new Date(System.currentTimeMillis()));
            query.setParameter("INV_CODE", item.getInvCode());
            query.setParameter("INV_QTY", item.getQty());
            query.setParameter("NOTES", request.getNotes());
            query.setParameter("USER_ID", request.getUserId());
            query.setParameter("INV_BRAND_CODE", item.getInvBrand());
            query.setParameter("INV_NAME", item.getInvName());
            query.setParameter("BARCODE", item.getBarcode());
            query.setParameter("INTERNAL_COUNT", item.getNotes());
            query.setParameter("WHS_CODE", request.getWhsCode());
            query.setParameter("EXP_CENTER_CODE", request.getExpCenterCode());
            query.executeUpdate();
        }
        StoredProcedureQuery procedureQuery = em.createStoredProcedureQuery("SP_CREATE_INTERNAL_USE_ORDER");
        procedureQuery.registerStoredProcedureParameter("USER_ID", String.class, IN);
        procedureQuery.registerStoredProcedureParameter("RESULT_ID", Integer.class, ParameterMode.OUT);
        procedureQuery.registerStoredProcedureParameter("ERROR_MESSAGE", String.class, ParameterMode.OUT);
        procedureQuery.setParameter("USER_ID", request.getUserId());
        procedureQuery.execute();
        em.close();

        int resultId = (int) procedureQuery.getOutputParameterValue("RESULT_ID");
        String errorMessage = String.valueOf(procedureQuery.getOutputParameterValue("ERROR_MESSAGE"));

        if (resultId == 1)
            throw new OperationNotCompletedException(errorMessage);
    }
}
