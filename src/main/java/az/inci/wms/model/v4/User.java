/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package az.inci.wms.model.v4;

import lombok.Data;

/**
 * @author User
 */

@Data
public class User {
    private String id;
    private String name;
    private String password;
    private String whsCode;
    private String pickGroup;
    private boolean collectFlag;
    private boolean pickFlag;
    private boolean checkFlag;
    private boolean countFlag;
    private boolean attributeFlag;
    private boolean locationFlag;
    private boolean packFlag;
    private boolean docFlag;
    private boolean loadingFlag;
    private boolean approveFlag;
    private boolean approvePrdFlag;
    private boolean purchaseOrdersFlag;
    private boolean barcodeFlag;
    private boolean archiveFlag;
    private boolean changeInvMasterFlag;

    public void setUserInfo(String field, boolean value) {
        switch (field) {
            case "CollectBtn":
                setCollectFlag(!value);
                break;
            case "pickDocsBtn":
                setPickFlag(!value);
                break;
            case "docsBtn":
                setDocFlag(!value);
                break;
            case "CheckPtBtn":
                setCheckFlag(!value);
                break;
            case "packBtn":
                setPackFlag(!value);
                break;
            case "CountingBtn":
                setCountFlag(!value);
                break;
            case "AttributeBtn":
                setAttributeFlag(!value);
                break;
            case "LocationBtn":
                setLocationFlag(!value);
                break;
            case "LoadingBtn":
                setLoadingFlag(!value);
                break;
            case "approveBtn":
                setApproveFlag(!value);
                break;
            case "approvePrdBtn":
                setApprovePrdFlag(!value);
                break;
            case "purchaseOrderBtn":
                setPurchaseOrdersFlag(!value);
                break;
            case "barcodeBtn":
                setBarcodeFlag(!value);
                break;
            case "archiveBtn":
                setArchiveFlag(!value);
                break;
            case "changeInvMasterFlag":
                setChangeInvMasterFlag(!value);
                break;
        }
    }
}
