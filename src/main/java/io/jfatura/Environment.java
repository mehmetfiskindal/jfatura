package io.jfatura;

/**
 * GİB e-Arşiv Portal ortamları.
 *
 * <p>PROD: https://earsivportal.efatura.gov.tr
 * <br>TEST: https://earsivportaltest.efatura.gov.tr
 */
public enum Environment {

    PROD("https://earsivportal.efatura.gov.tr", "anologin", "anologin"),
    TEST("https://earsivportaltest.efatura.gov.tr", "login", "logout");

    private final String baseUrl;
    private final String loginCmd;
    private final String logoutCmd;

    Environment(String baseUrl, String loginCmd, String logoutCmd) {
        this.baseUrl = baseUrl;
        this.loginCmd = loginCmd;
        this.logoutCmd = logoutCmd;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String loginCmd() {
        return loginCmd;
    }

    public String logoutCmd() {
        return logoutCmd;
    }
}
