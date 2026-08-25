package io.jfatura;

import static org.assertj.core.api.Assertions.assertThat;

import io.jfatura.util.LogMasking;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("util/LogMasking")
class LogMaskingTest {

    @Test
    @DisplayName("sifre değeri maskelenir")
    void masksSifre() {
        assertThat(LogMasking.maskFormBody("assoscmd=anologin&sifre=gizli123&sifre2=gizli123"))
                .isEqualTo("assoscmd=anologin&sifre=***&sifre2=***");
    }

    @Test
    @DisplayName("userid ve token değerleri maskelenir")
    void masksUseridAndToken() {
        String masked = LogMasking.maskFormBody("userid=admin&token=oturum-abc&jp=%7B%7D");
        assertThat(masked).isEqualTo("userid=***&token=***&jp=%7B%7D");
    }

    @Test
    @DisplayName("hassas olmayan alanlar korunur")
    void keepsNonSensitive() {
        assertThat(LogMasking.maskFormBody("cmd=EARSIV_PORTAL_FATURA_OLUSTUR&pageName=RG_BASITFATURA"))
                .isEqualTo("cmd=EARSIV_PORTAL_FATURA_OLUSTUR&pageName=RG_BASITFATURA");
    }

    @Test
    @DisplayName("null ve boş gövde olduğu gibi döner")
    void nullAndEmptyPassThrough() {
        assertThat(LogMasking.maskFormBody(null)).isNull();
        assertThat(LogMasking.maskFormBody("")).isEmpty();
    }

    @Test
    @DisplayName("uzun token kısaltılarak maskelenir")
    void tokenSummary() {
        assertThat(LogMasking.maskToken("abcdefghij12345678")).isEqualTo("abcd***5678");
    }

    @Test
    @DisplayName("kısa veya boş token tamamen maskelenir")
    void shortTokenFullyMasked() {
        assertThat(LogMasking.maskToken("abc")).isEqualTo("***");
        assertThat(LogMasking.maskToken(null)).isEqualTo("***");
        assertThat(LogMasking.maskToken("")).isEqualTo("***");
    }
}
