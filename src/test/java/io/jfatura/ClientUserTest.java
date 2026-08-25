package io.jfatura;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jfatura.model.UserData;
import io.jfatura.support.Fixtures;
import io.jfatura.support.GibHttpMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@DisplayName("FaturaClient — user operations")
class ClientUserTest {

    private FaturaClient client;
    private GibHttpMock gib;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        gib = new GibHttpMock();
        client = new FaturaClient(Environment.PROD, RestClient.builder().requestFactory(gib));
        mapper = new ObjectMapper();
    }

    private String rawUserJson() {
        try {
            return mapper.writeValueAsString(Fixtures.rawUserData());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // ─── getUserData ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getUserData")
    class GetUserData {

        @Test
        @DisplayName("sends the correct GIB command")
        void command() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            client.getUserData(Fixtures.TOKEN);
            assertThat(gib.call(0).cmd()).isEqualTo("EARSIV_PORTAL_KULLANICI_BILGILERI_GETIR");
        }

        @Test
        @DisplayName("sends the correct pageName")
        void pageName() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            client.getUserData(Fixtures.TOKEN);
            assertThat(gib.call(0).pageName()).isEqualTo("RG_KULLANICI");
        }

        @Test
        @DisplayName("maps vknTckn → taxIDOrTRID")
        void vknTckn() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).taxIDOrTRID())
                    .isEqualTo(Fixtures.rawUserData().vknTckn());
        }

        @Test
        @DisplayName("maps unvan → title")
        void unvan() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).title()).isEqualTo(Fixtures.rawUserData().unvan());
        }

        @Test
        @DisplayName("maps ad → name")
        void ad() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).name()).isEqualTo(Fixtures.rawUserData().ad());
        }

        @Test
        @DisplayName("maps soyad → surname")
        void soyad() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).surname()).isEqualTo(Fixtures.rawUserData().soyad());
        }

        @Test
        @DisplayName("maps sicilNo → registryNo")
        void sicilNo() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).registryNo())
                    .isEqualTo(Fixtures.rawUserData().sicilNo());
        }

        @Test
        @DisplayName("maps mersisNo → mersisNo")
        void mersisNo() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).mersisNo())
                    .isEqualTo(Fixtures.rawUserData().mersisNo());
        }

        @Test
        @DisplayName("maps vergiDairesi → taxOffice")
        void vergiDairesi() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).taxOffice())
                    .isEqualTo(Fixtures.rawUserData().vergiDairesi());
        }

        @Test
        @DisplayName("maps cadde → fullAddress")
        void cadde() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).fullAddress())
                    .isEqualTo(Fixtures.rawUserData().cadde());
        }

        @Test
        @DisplayName("maps apartmanAdi → buildingName")
        void apartmanAdi() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).buildingName())
                    .isEqualTo(Fixtures.rawUserData().apartmanAdi());
        }

        @Test
        @DisplayName("maps apartmanNo → buildingNumber")
        void apartmanNo() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).buildingNumber())
                    .isEqualTo(Fixtures.rawUserData().apartmanNo());
        }

        @Test
        @DisplayName("maps kapiNo → doorNumber")
        void kapiNo() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).doorNumber())
                    .isEqualTo(Fixtures.rawUserData().kapiNo());
        }

        @Test
        @DisplayName("maps kasaba → town")
        void kasaba() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).town()).isEqualTo(Fixtures.rawUserData().kasaba());
        }

        @Test
        @DisplayName("maps ilce → district")
        void ilce() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).district()).isEqualTo(Fixtures.rawUserData().ilce());
        }

        @Test
        @DisplayName("maps il → city")
        void il() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).city()).isEqualTo(Fixtures.rawUserData().il());
        }

        @Test
        @DisplayName("maps postaKodu → zipCode")
        void postaKodu() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).zipCode())
                    .isEqualTo(Fixtures.rawUserData().postaKodu());
        }

        @Test
        @DisplayName("maps ulke → country")
        void ulke() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).country()).isEqualTo(Fixtures.rawUserData().ulke());
        }

        @Test
        @DisplayName("maps telNo → phoneNumber")
        void telNo() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).phoneNumber())
                    .isEqualTo(Fixtures.rawUserData().telNo());
        }

        @Test
        @DisplayName("maps faksNo → faxNumber")
        void faksNo() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).faxNumber())
                    .isEqualTo(Fixtures.rawUserData().faksNo());
        }

        @Test
        @DisplayName("maps ePostaAdresi → email")
        void ePostaAdresi() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).email())
                    .isEqualTo(Fixtures.rawUserData().ePostaAdresi());
        }

        @Test
        @DisplayName("maps webSitesiAdresi → webSite")
        void webSitesiAdresi() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).webSite())
                    .isEqualTo(Fixtures.rawUserData().webSitesiAdresi());
        }

        @Test
        @DisplayName("maps isMerkezi → businessCenter")
        void isMerkezi() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN).businessCenter())
                    .isEqualTo(Fixtures.rawUserData().isMerkezi());
        }

        @Test
        @DisplayName("returns an object with all UserData fields populated")
        void allFieldsPopulated() {
            gib.once("{\"data\":" + rawUserJson() + "}");
            assertThat(client.getUserData(Fixtures.TOKEN)).isEqualTo(Fixtures.userData());
        }
    }

    // ─── updateUserData ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateUserData")
    class UpdateUserData {

        private final UserData userData = Fixtures.userData();

        @Test
        @DisplayName("sends the correct GIB command")
        void command() {
            gib.once("{\"data\":\"ok\"}");
            client.updateUserData(Fixtures.TOKEN, userData);
            assertThat(gib.call(0).cmd()).isEqualTo("EARSIV_PORTAL_KULLANICI_BILGILERI_KAYDET");
        }

        @Test
        @DisplayName("sends the correct pageName")
        void pageName() {
            gib.once("{\"data\":\"ok\"}");
            client.updateUserData(Fixtures.TOKEN, userData);
            assertThat(gib.call(0).pageName()).isEqualTo("RG_KULLANICI");
        }

        @Test
        @DisplayName("maps taxIDOrTRID → vknTckn")
        void taxIdMapping() {
            gib.once("{\"data\":\"ok\"}");
            client.updateUserData(Fixtures.TOKEN, userData);
            assertThat(gib.call(0).jp().get("vknTckn")).isEqualTo(userData.taxIDOrTRID());
        }

        @Test
        @DisplayName("maps title → unvan")
        void titleMapping() {
            gib.once("{\"data\":\"ok\"}");
            client.updateUserData(Fixtures.TOKEN, userData);
            assertThat(gib.call(0).jp().get("unvan")).isEqualTo(userData.title());
        }

        @Test
        @DisplayName("maps name → ad")
        void nameMapping() {
            gib.once("{\"data\":\"ok\"}");
            client.updateUserData(Fixtures.TOKEN, userData);
            assertThat(gib.call(0).jp().get("ad")).isEqualTo(userData.name());
        }

        @Test
        @DisplayName("maps surname → soyad")
        void surnameMapping() {
            gib.once("{\"data\":\"ok\"}");
            client.updateUserData(Fixtures.TOKEN, userData);
            assertThat(gib.call(0).jp().get("soyad")).isEqualTo(userData.surname());
        }

        @Test
        @DisplayName("maps taxOffice → vergiDairesi")
        void taxOfficeMapping() {
            gib.once("{\"data\":\"ok\"}");
            client.updateUserData(Fixtures.TOKEN, userData);
            assertThat(gib.call(0).jp().get("vergiDairesi")).isEqualTo(userData.taxOffice());
        }

        @Test
        @DisplayName("maps fullAddress → cadde")
        void fullAddressMapping() {
            gib.once("{\"data\":\"ok\"}");
            client.updateUserData(Fixtures.TOKEN, userData);
            assertThat(gib.call(0).jp().get("cadde")).isEqualTo(userData.fullAddress());
        }

        @Test
        @DisplayName("maps city → il")
        void cityMapping() {
            gib.once("{\"data\":\"ok\"}");
            client.updateUserData(Fixtures.TOKEN, userData);
            assertThat(gib.call(0).jp().get("il")).isEqualTo(userData.city());
        }

        @Test
        @DisplayName("maps email → ePostaAdresi")
        void emailMapping() {
            gib.once("{\"data\":\"ok\"}");
            client.updateUserData(Fixtures.TOKEN, userData);
            assertThat(gib.call(0).jp().get("ePostaAdresi")).isEqualTo(userData.email());
        }

        @Test
        @DisplayName("maps phoneNumber → telNo")
        void phoneMapping() {
            gib.once("{\"data\":\"ok\"}");
            client.updateUserData(Fixtures.TOKEN, userData);
            assertThat(gib.call(0).jp().get("telNo")).isEqualTo(userData.phoneNumber());
        }

        @Test
        @DisplayName("maps businessCenter → isMerkezi")
        void businessCenterMapping() {
            gib.once("{\"data\":\"ok\"}");
            client.updateUserData(Fixtures.TOKEN, userData);
            assertThat(gib.call(0).jp().get("isMerkezi")).isEqualTo(userData.businessCenter());
        }

        @Test
        @DisplayName("returns result.data")
        void returnsData() {
            gib.once("{\"data\":{\"updated\":true}}");
            var result = client.updateUserData(Fixtures.TOKEN, userData);
            assertThat(result.get("updated").asBoolean()).isTrue();
        }
    }
}
