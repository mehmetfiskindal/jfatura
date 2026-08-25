package io.jfatura.command;

/**
 * GİB dispatch komutları: her işlem bir {@code [cmd, pageName]} ikilisidir.
 */
public enum GibCommand {
    CREATE_DRAFT_INVOICE("EARSIV_PORTAL_FATURA_OLUSTUR", "RG_BASITFATURA"),
    GET_ALL_INVOICES_BY_DATE_RANGE("EARSIV_PORTAL_TASLAKLARI_GETIR", "RG_BASITTASLAKLAR"),
    GET_ALL_INVOICES_ISSUED_TO_ME_BY_DATE_RANGE("EARSIV_PORTAL_ADIMA_KESILEN_BELGELERI_GETIR", "RG_ALICI_TASLAKLAR"),
    SIGN_DRAFT_INVOICE("EARSIV_PORTAL_FATURA_HSM_CIHAZI_ILE_IMZALA", "RG_BASITTASLAKLAR"),
    GET_INVOICE_HTML("EARSIV_PORTAL_FATURA_GOSTER", "RG_BASITTASLAKLAR"),
    CANCEL_DRAFT_INVOICE("EARSIV_PORTAL_FATURA_SIL", "RG_BASITTASLAKLAR"),
    GET_RECIPIENT_DATA_BY_TAX_ID_OR_TRID("SICIL_VEYA_MERNISTEN_BILGILERI_GETIR", "RG_BASITFATURA"),
    GET_SIGN_PHONE_NUMBER("EARSIV_PORTAL_TELEFONNO_SORGULA", "RG_SMSONAY"),
    SEND_SIGN_SMS_CODE("EARSIV_PORTAL_SMSSIFRE_GONDER", "RG_SMSONAY"),
    /**
     * İmzalama komutu. GİB bu servisi okunabilir addan opak bir kimliğe taşıdı;
     * {@code EARSIV_PORTAL_SMSSIFRE_DOGRULA} canlı portalda "Service Not Found" döner.
     */
    VERIFY_SMS_CODE("0lhozfib5410mp", "RG_SMSONAY"),
    GET_USER_DATA("EARSIV_PORTAL_KULLANICI_BILGILERI_GETIR", "RG_KULLANICI"),
    UPDATE_USER_DATA("EARSIV_PORTAL_KULLANICI_BILGILERI_KAYDET", "RG_KULLANICI");

    private final String cmd;
    private final String pageName;

    GibCommand(String cmd, String pageName) {
        this.cmd = cmd;
        this.pageName = pageName;
    }

    public String cmd() {
        return cmd;
    }

    public String pageName() {
        return pageName;
    }
}
