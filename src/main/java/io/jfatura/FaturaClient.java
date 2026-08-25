package io.jfatura;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jfatura.api.ApiResponse;
import io.jfatura.api.GibApiMessage;
import io.jfatura.command.GibCommand;
import io.jfatura.exception.GibApiException;
import io.jfatura.exception.GibAuthException;
import io.jfatura.exception.GibDraftException;
import io.jfatura.mapper.InvoicePayloadMapper;
import io.jfatura.model.CreateInvoiceResult;
import io.jfatura.model.DateRange;
import io.jfatura.model.DraftInvoice;
import io.jfatura.model.InvoiceDetails;
import io.jfatura.model.InvoiceListItem;
import io.jfatura.model.LogoutResult;
import io.jfatura.model.RawUserData;
import io.jfatura.model.RecipientData;
import io.jfatura.model.UserData;
import io.jfatura.util.LogMasking;
import io.jfatura.util.UriEncode;
import io.jfatura.util.Uuids;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * GİB e-Arşiv Portal istemcisi. Tüm işlemler bu sınıf üzerinden yapılır.
 *
 * <p>Örnek:
 * {@snippet :
 * FaturaClient client = FaturaClient.create();            // PROD
 * FaturaClient testClient = FaturaClient.create(Environment.TEST);
 * }
 *
 * <p>Spring dışı kullanımda timeout/retry/cache ayarları için:
 * {@snippet :
 * FaturaClient client = FaturaClient.builder()
 *         .environment(Environment.PROD)
 *         .connectTimeout(Duration.ofSeconds(10))
 *         .loginRetry(5, Duration.ofSeconds(3))
 *         .tokenCache(Duration.ofMinutes(30))
 *         .build();
 * }
 */
public class FaturaClient {

    private static final Logger log = LoggerFactory.getLogger(FaturaClient.class);
    private static final Pattern DRAFT_CREATED =
            Pattern.compile("başarıyla oluşturulmuştur", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern SESSION_LOCKED = Pattern.compile("birden fazla giriş|Güvenli Çıkış");
    private static final MediaType FORM_URLENCODED_UTF8 =
            MediaType.parseMediaType("application/x-www-form-urlencoded;charset=UTF-8");
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    private final Environment environment;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final int loginMaxAttempts;
    private final Duration loginRetryDelay;
    private final @Nullable TokenCache tokenCache;
    private final Clock clock;

    public FaturaClient() {
        this(Environment.PROD);
    }

    public FaturaClient(Environment environment) {
        this(environment, RestClient.builder().requestFactory(defaultRequestFactory(null, null)));
    }

    public FaturaClient(Environment environment, RestClient.Builder builder) {
        this(environment, builder, new ObjectMapper());
    }

    public FaturaClient(Environment environment, RestClient.Builder builder, ObjectMapper objectMapper) {
        this(environment, builder, objectMapper, 1, Duration.ZERO, null, Clock.systemUTC());
    }

    FaturaClient(
            Environment environment,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            int loginMaxAttempts,
            Duration loginRetryDelay,
            @Nullable TokenCache tokenCache,
            Clock clock) {
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.loginMaxAttempts = Math.max(1, loginMaxAttempts);
        this.loginRetryDelay = loginRetryDelay == null ? Duration.ZERO : loginRetryDelay;
        this.tokenCache = tokenCache;
        this.clock = clock;
        this.restClient = restClientBuilder
                .defaultHeaders(headers -> {
                    headers.set("accept", "*/*");
                    headers.set("accept-language", "tr,en-US;q=0.9,en;q=0.8");
                    headers.set("cache-control", "no-cache");
                    headers.set("pragma", "no-cache");
                    headers.set("sec-fetch-mode", "cors");
                    headers.set("sec-fetch-site", "same-origin");
                })
                .build();
    }

    private static ClientHttpRequestFactory defaultRequestFactory(
            @Nullable Duration connectTimeout, @Nullable Duration readTimeout) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(java.net.http.HttpClient.newBuilder()
                .connectTimeout(connectTimeout != null ? connectTimeout : DEFAULT_CONNECT_TIMEOUT)
                .build());
        factory.setReadTimeout(readTimeout != null ? readTimeout : DEFAULT_READ_TIMEOUT);
        return factory;
    }

    public static FaturaClient create() {
        return builder().environment(Environment.PROD).build();
    }

    public static FaturaClient create(Environment environment) {
        return builder().environment(environment).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    Environment environment() {
        return environment;
    }

    String baseUrl() {
        return environment.baseUrl();
    }

    String loginCmd() {
        return environment.loginCmd();
    }

    String logoutCmd() {
        return environment.logoutCmd();
    }

    int loginMaxAttempts() {
        return loginMaxAttempts;
    }

    // ─── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder {

        private Environment environment = Environment.PROD;
        private RestClient.@Nullable Builder restClientBuilder;
        private @Nullable ObjectMapper objectMapper;
        private @Nullable Duration connectTimeout;
        private @Nullable Duration readTimeout;
        private int loginMaxAttempts = 1;
        private Duration loginRetryDelay = Duration.ofSeconds(3);
        private @Nullable Duration tokenCacheTtl;
        private Clock clock = Clock.systemUTC();

        private Builder() {}

        public Builder environment(Environment environment) {
            this.environment = Objects.requireNonNull(environment, "environment");
            return this;
        }

        /** Verilirse transport (timeout dâhil) tamamen bu builder'a devredilir. */
        public Builder restClient(RestClient.Builder restClientBuilder) {
            this.restClientBuilder = restClientBuilder;
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = requirePositive(connectTimeout, "connectTimeout");
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = requirePositive(readTimeout, "readTimeout");
            return this;
        }

        /**
         * Oturum kilidi ("birden fazla giriş" / "Güvenli Çıkış") hatalarında
         * tekrar deneme. Kamu/ortak test hesapları için önerilir.
         */
        public Builder loginRetry(int maxAttempts, Duration delay) {
            if (maxAttempts < 1) {
                throw new IllegalArgumentException("maxAttempts >= 1 olmalıdır");
            }
            this.loginMaxAttempts = maxAttempts;
            this.loginRetryDelay = delay == null ? Duration.ZERO : delay;
            return this;
        }

        /** Oturum token'ını kimlik bazlı önbelleğe alır; logout invalidates eder. */
        public Builder tokenCache(Duration ttl) {
            this.tokenCacheTtl = requirePositive(ttl, "ttl");
            return this;
        }

        public Builder clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock");
            return this;
        }

        public FaturaClient build() {
            RestClient.Builder rb = restClientBuilder != null
                    ? restClientBuilder
                    : RestClient.builder().requestFactory(defaultRequestFactory(connectTimeout, readTimeout));
            TokenCache cache = tokenCacheTtl != null ? new TokenCache(tokenCacheTtl, clock) : null;
            return new FaturaClient(
                    environment,
                    rb,
                    objectMapper != null ? objectMapper : new ObjectMapper(),
                    loginMaxAttempts,
                    loginRetryDelay,
                    cache,
                    clock);
        }

        private static Duration requirePositive(Duration value, String name) {
            if (value == null || value.isNegative() || value.isZero()) {
                throw new IllegalArgumentException(name + " pozitif bir süre olmalıdır");
            }
            return value;
        }
    }

    // ─── Token cache ───────────────────────────────────────────────────────────

    static final class TokenCache {

        private record Cached(String token, Instant expiresAt) {}

        private final Duration ttl;
        private final Clock clock;
        private final ConcurrentHashMap<String, Cached> store = new ConcurrentHashMap<>();

        TokenCache(Duration ttl, Clock clock) {
            this.ttl = ttl;
            this.clock = clock;
        }

        Optional<String> get(String userName, String password) {
            Cached cached = store.get(key(userName, password));
            if (cached == null) {
                return Optional.empty();
            }
            if (!cached.expiresAt().isAfter(clock.instant())) {
                store.remove(key(userName, password));
                return Optional.empty();
            }
            return Optional.of(cached.token());
        }

        void put(String userName, String password, String token) {
            store.put(key(userName, password), new Cached(token, clock.instant().plus(ttl)));
        }

        void invalidateToken(String token) {
            store.entrySet().removeIf(entry -> entry.getValue().token().equals(token));
        }

        int size() {
            return store.size();
        }

        private static String key(String userName, String password) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest((userName + ":" + password).getBytes(StandardCharsets.UTF_8));
                return HexFormat.of().formatHex(hash);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 kullanılamıyor", e);
            }
        }
    }

    // ─── Core HTTP ─────────────────────────────────────────────────────────────

    private ApiResponse runCommand(String token, GibCommand command, Object data) {
        log.debug(
                "dispatch cmd={} pageName={} token={}", command.cmd(), command.pageName(), LogMasking.maskToken(token));
        return runCommand(token, command.cmd(), command.pageName(), data);
    }

    private ApiResponse runCommand(String token, String cmd, String pageName, Object data) {
        String jp;
        try {
            jp = objectMapper.writeValueAsString(data == null ? Map.of() : data);
        } catch (Exception e) {
            throw new IllegalStateException("GİB isteği serileştirilemedi", e);
        }
        String body = "cmd=" + cmd
                + "&callid=" + Uuids.newCallId()
                + "&pageName=" + pageName
                + "&token=" + token
                + "&jp=" + UriEncode.encodeURIComponent(jp);
        String json = postForm(environment.baseUrl() + "/earsiv-services/dispatch", body);
        ApiResponse response = parse(json);
        assertApiSuccess(response, false);
        return response;
    }

    private String postForm(String url, String body) {
        log.trace("POST {} body={}", url, LogMasking.maskFormBody(body));
        return restClient
                .method(HttpMethod.POST)
                .uri(URI.create(url))
                .contentType(FORM_URLENCODED_UTF8)
                .body(body)
                .exchange((request, response) -> new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8));
    }

    private ApiResponse parse(String json) {
        try {
            return objectMapper.readValue(json, ApiResponse.class);
        } catch (Exception e) {
            throw new GibApiException("GİB yanıtı JSON olarak ayrıştırılamadı: " + e.getMessage());
        }
    }

    /** GİB API hata mesajını okur ve varsa uygun istisnayı fırlatır. */
    private static void assertApiSuccess(ApiResponse response, boolean auth) {
        if (response.hasError()) {
            String text = firstMessageText(response);
            GibApiException exception = auth
                    ? new GibAuthException(text, response.error(), messageTexts(response))
                    : new GibApiException(text, response.error(), messageTexts(response));
            log.debug("GİB hatası errorCode={} mesaj={}", response.error(), text);
            throw exception;
        }
    }

    private static List<String> messageTexts(ApiResponse response) {
        return response.messages().stream().map(GibApiMessage::text).collect(Collectors.toList());
    }

    private static String firstMessageText(ApiResponse response) {
        if (response.messages().isEmpty()) {
            return "GİB API hatası";
        }
        String text = response.messages().get(0).text();
        return text == null || text.isEmpty() ? "GİB API hatası" : text;
    }

    /**
     * GİB taslak oluşturma cevabını doğrular.
     *
     * <p>{@code EARSIV_PORTAL_FATURA_OLUSTUR} hata durumunda da {@code error}
     * alanı göndermez; sonucu yalnızca {@code data} içindeki serbest metinden
     * anlaşılır. Bu yüzden {@code assertApiSuccess} bu komut için yeterli değildir.
     */
    private static void assertDraftCreated(ApiResponse response) {
        String text = response.data() != null && response.data().isTextual()
                ? response.data().textValue()
                : "";
        Matcher matcher = DRAFT_CREATED.matcher(text != null ? text : "");
        if (!matcher.find()) {
            String message = text == null || text.isEmpty() ? "GİB taslak faturayı oluşturmadı" : text;
            throw new GibDraftException(message, response.error());
        }
    }

    // ─── Auth ──────────────────────────────────────────────────────────────────

    public String getToken(String userName, String password) {
        if (tokenCache != null) {
            Optional<String> cached = tokenCache.get(userName, password);
            if (cached.isPresent()) {
                log.debug("getToken cache HIT user={} ", userName);
                return cached.get();
            }
        }
        String token = getTokenWithRetry(userName, password);
        if (tokenCache != null) {
            tokenCache.put(userName, password, token);
        }
        return token;
    }

    private String getTokenWithRetry(String userName, String password) {
        for (int attempt = 1; attempt <= loginMaxAttempts; attempt++) {
            try {
                return requestToken(userName, password);
            } catch (GibAuthException exception) {
                boolean locked = exception.getMessage() != null
                        && SESSION_LOCKED.matcher(exception.getMessage()).find();
                if (!locked || attempt == loginMaxAttempts) {
                    throw exception;
                }
                log.warn(
                        "Oturum kilidi algılandı (deneme {}/{}), {} ms sonra tekrar denenecek: {}",
                        attempt,
                        loginMaxAttempts,
                        loginRetryDelay.toMillis(),
                        exception.getMessage());
                sleepQuietly(loginRetryDelay);
            }
        }
        throw new IllegalStateException("ulaşılmaz");
    }

    private static void sleepQuietly(Duration duration) {
        if (duration.isNegative() || duration.isZero()) {
            return;
        }
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new GibAuthException("Login retry beklemesi kesildi");
        }
    }

    private String requestToken(String userName, String password) {
        String body = "assoscmd=" + environment.loginCmd()
                + "&rtype=json&userid=" + userName
                + "&sifre=" + password
                + "&sifre2=" + password
                + "&parola=1&";
        String json = postForm(environment.baseUrl() + "/earsiv-services/assos-login", body);
        ApiResponse response = parse(json);
        assertApiSuccess(response, true);
        return Objects.requireNonNull(response.token(), "GİB oturum token'ı döndürmedi");
    }

    public LogoutResult logout(String token) {
        String body = "assoscmd=" + environment.logoutCmd() + "&rtype=json&token=" + token + "&";
        String json = postForm(environment.baseUrl() + "/earsiv-services/assos-login", body);
        LogoutResult result = LogoutResult.fromJson(parse(json).data());
        if (tokenCache != null) {
            tokenCache.invalidateToken(token);
        }
        return result;
    }

    // ─── Invoice CRUD ──────────────────────────────────────────────────────────

    public DraftInvoice createDraftInvoice(String token, InvoiceDetails invoiceDetails) {
        Map<String, Object> invoiceData = InvoicePayloadMapper.toPayload(invoiceDetails);

        // ETTN sunucu tarafında atandığı için, oluşturmadan önce/sonra o güne ait
        // taslak listesi karşılaştırılarak yeni kayıt bulunur.
        DateRange day = DateRange.of(invoiceDetails.date(), invoiceDetails.date());
        Set<String> before = getAllInvoicesByDateRange(token, day).stream()
                .map(InvoiceListItem::ettn)
                .collect(Collectors.toSet());

        ApiResponse invoice = runCommand(token, GibCommand.CREATE_DRAFT_INVOICE, invoiceData);
        assertDraftCreated(invoice);

        InvoiceListItem created = getAllInvoicesByDateRange(token, day).stream()
                .filter(item -> !before.contains(item.ettn()))
                .findFirst()
                .orElse(null);

        return new DraftInvoice(
                invoiceDetails.date(),
                created != null ? created.ettn() : "",
                created != null ? created.belgeNumarasi() : null,
                created,
                invoice);
    }

    public Optional<InvoiceListItem> findInvoice(String token, DraftInvoice draftInvoice) {
        return getAllInvoicesByDateRange(token, DateRange.of(draftInvoice.date(), draftInvoice.date())).stream()
                .filter(inv -> inv.ettn().equals(draftInvoice.uuid()))
                .findFirst();
    }

    /** ☢️ İmzalama kesilmiş sayılan mali işlem oluşturur; dikkatli kullanın. */
    public ApiResponse signDraftInvoice(String token, InvoiceListItem draftInvoice) {
        return runCommand(token, GibCommand.SIGN_DRAFT_INVOICE, Map.of("imzalanacaklar", List.of(draftInvoice)));
    }

    public String cancelDraftInvoice(String token, String reason, InvoiceListItem draftInvoice) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("silinecekler", List.of(draftInvoice));
        payload.put("aciklama", reason);
        ApiResponse result = runCommand(token, GibCommand.CANCEL_DRAFT_INVOICE, payload);
        return dataAsText(result.data());
    }

    // ─── Queries ───────────────────────────────────────────────────────────────

    public List<InvoiceListItem> getAllInvoicesByDateRange(String token, DateRange range) {
        return queryInvoices(token, GibCommand.GET_ALL_INVOICES_BY_DATE_RANGE, range);
    }

    public List<InvoiceListItem> getAllInvoicesIssuedToMeByDateRange(String token, DateRange range) {
        return queryInvoices(token, GibCommand.GET_ALL_INVOICES_ISSUED_TO_ME_BY_DATE_RANGE, range);
    }

    private List<InvoiceListItem> queryInvoices(String token, GibCommand command, DateRange range) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("baslangic", range.startDate());
        payload.put("bitis", range.endDate());
        payload.put("hangiTip", "5000/30000");
        payload.put("table", List.of());
        ApiResponse result = runCommand(token, command, payload);
        return toInvoiceList(result.data());
    }

    private List<InvoiceListItem> toInvoiceList(@Nullable JsonNode data) {
        if (data == null || data.isNull() || data.isMissingNode()) {
            return List.of();
        }
        return objectMapper.convertValue(data, new TypeReference<List<InvoiceListItem>>() {});
    }

    // ─── Download & HTML ───────────────────────────────────────────────────────

    public String getInvoiceHTML(String token, String uuid, boolean signed) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ettn", uuid);
        payload.put("onayDurumu", signed ? "Onaylandı" : "Onaylanmadı");
        ApiResponse result = runCommand(token, GibCommand.GET_INVOICE_HTML, payload);
        return dataAsText(result.data());
    }

    public String getDownloadURL(String token, String invoiceUUID, boolean signed) {
        return environment.baseUrl() + "/earsiv-services/download"
                + "?token=" + token
                + "&ettn=" + invoiceUUID
                + "&belgeTip=FATURA"
                + "&onayDurumu=" + UriEncode.encodeURIComponent(signed ? "Onaylandı" : "Onaylanmadı")
                + "&cmd=downloadResource&";
    }

    // ─── User ──────────────────────────────────────────────────────────────────

    public UserData getUserData(String token) {
        ApiResponse result = runCommand(token, GibCommand.GET_USER_DATA, Map.of());
        RawUserData d = objectMapper.convertValue(result.data(), RawUserData.class);
        return new UserData(
                d.vknTckn(),
                d.unvan(),
                d.ad(),
                d.soyad(),
                d.sicilNo(),
                d.mersisNo(),
                d.vergiDairesi(),
                d.cadde(),
                d.apartmanAdi(),
                d.apartmanNo(),
                d.kapiNo(),
                d.kasaba(),
                d.ilce(),
                d.il(),
                d.postaKodu(),
                d.ulke(),
                d.telNo(),
                d.faksNo(),
                d.ePostaAdresi(),
                d.webSitesiAdresi(),
                d.isMerkezi());
    }

    /**
     * Portal kullanıcı bilgilerini günceller. GİB'in yanıt {@code data}
     * alanının şekli hesaba göre değişebildiğinden ham {@link JsonNode} döner.
     */
    public @Nullable JsonNode updateUserData(String token, UserData userData) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfNotNull(payload, "vknTckn", userData.taxIDOrTRID());
        putIfNotNull(payload, "unvan", userData.title());
        putIfNotNull(payload, "ad", userData.name());
        putIfNotNull(payload, "soyad", userData.surname());
        putIfNotNull(payload, "sicilNo", userData.registryNo());
        putIfNotNull(payload, "mersisNo", userData.mersisNo());
        putIfNotNull(payload, "vergiDairesi", userData.taxOffice());
        putIfNotNull(payload, "cadde", userData.fullAddress());
        putIfNotNull(payload, "apartmanAdi", userData.buildingName());
        putIfNotNull(payload, "apartmanNo", userData.buildingNumber());
        putIfNotNull(payload, "kapiNo", userData.doorNumber());
        putIfNotNull(payload, "kasaba", userData.town());
        putIfNotNull(payload, "ilce", userData.district());
        putIfNotNull(payload, "il", userData.city());
        putIfNotNull(payload, "postaKodu", userData.zipCode());
        putIfNotNull(payload, "ulke", userData.country());
        putIfNotNull(payload, "telNo", userData.phoneNumber());
        putIfNotNull(payload, "faksNo", userData.faxNumber());
        putIfNotNull(payload, "ePostaAdresi", userData.email());
        putIfNotNull(payload, "webSitesiAdresi", userData.webSite());
        putIfNotNull(payload, "isMerkezi", userData.businessCenter());
        ApiResponse result = runCommand(token, GibCommand.UPDATE_USER_DATA, payload);
        return result.data();
    }

    // ─── Recipient ─────────────────────────────────────────────────────────────

    public RecipientData getRecipientDataByTaxIDOrTRID(String token, String taxIDOrTRID) {
        // GİB parametre adındaki çift-n tuhaflığına dikkat: vknTcknn
        ApiResponse result =
                runCommand(token, GibCommand.GET_RECIPIENT_DATA_BY_TAX_ID_OR_TRID, Map.of("vknTcknn", taxIDOrTRID));
        return RecipientData.fromJson(objectMapper, result.data());
    }

    // ─── SMS ───────────────────────────────────────────────────────────────────

    /**
     * İmza SMS'inin gönderileceği <b>kayıtlı</b> cep telefonunu döner.
     *
     * <p>e-Arşiv portalı SMS'i şirket yetkilisinin sisteme kayıtlı numarasına
     * gönderir; numara dışarıdan verilmez. {@link #sendSignSMSCode} çağrılmadan
     * önce numara buradan alınmalıdır.
     */
    public @Nullable String getSignPhoneNumber(String token) {
        ApiResponse result = runCommand(token, GibCommand.GET_SIGN_PHONE_NUMBER, Map.of());
        return textField(result.data(), "telefon");
    }

    public @Nullable String sendSignSMSCode(String token, String phone) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("CEPTEL", phone);
        payload.put("KCEPTEL", false);
        payload.put("TIP", "");
        ApiResponse result = runCommand(token, GibCommand.SEND_SIGN_SMS_CODE, payload);
        String fromData = textField(result.data(), "oid");
        // GİB oid'yi data içinde döndürür; üst seviye alan yedek olarak okunur.
        return fromData != null ? fromData : result.oid();
    }

    /**
     * SMS kodunu doğrular <b>ve {@code invoices} listesindeki faturaları imzalar</b>.
     *
     * <p>☢️ İmzalama mali işlem oluşturur.
     *
     * <p>{@code OPR: 1} ve {@code DATA} alanları zorunludur: bunlar olmadan GİB
     * SMS kodunu doğrular ama hiçbir faturayı imzalamaz.
     *
     * @return GİB faturaları imzaladıysa {@code true} ({@code data.sonuc === "1"})
     */
    public boolean verifySignSMSCode(String token, String smsCode, String operationId) {
        return verifySignSMSCode(token, smsCode, operationId, List.of());
    }

    public boolean verifySignSMSCode(String token, String smsCode, String operationId, List<InvoiceListItem> invoices) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("SIFRE", smsCode);
        payload.put("OID", operationId);
        payload.put("OPR", 1);
        payload.put("DATA", invoices);
        ApiResponse result = runCommand(token, GibCommand.VERIFY_SMS_CODE, payload);
        // GİB sonucu data.sonuc alanında bildirir: "1" imzalandı demektir.
        return "1".equals(textField(result.data(), "sonuc"));
    }

    private static @Nullable String textField(@Nullable JsonNode data, String field) {
        if (data == null || !data.isObject()) {
            return null;
        }
        JsonNode value = data.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static String dataAsText(@Nullable JsonNode data) {
        if (data == null || data.isNull() || data.isMissingNode()) {
            return "";
        }
        return data.isTextual() ? data.textValue() : data.toString();
    }

    private static void putIfNotNull(Map<String, Object> map, String key, @Nullable Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    // ─── High-level composite ──────────────────────────────────────────────────

    public CreateInvoiceResult createInvoice(String userId, String password, InvoiceDetails invoiceDetails) {
        return createInvoice(userId, password, invoiceDetails, true);
    }

    public CreateInvoiceResult createInvoice(
            String userId, String password, InvoiceDetails invoiceDetails, boolean sign) {
        String token = getToken(userId, password);
        DraftInvoice draft = createDraftInvoice(token, invoiceDetails);
        Optional<InvoiceListItem> details = findInvoice(token, draft);
        if (sign && details.isPresent()) {
            signDraftInvoice(token, details.get());
        }
        return new CreateInvoiceResult(token, draft.uuid(), sign);
    }

    public String createInvoiceAndGetDownloadURL(String userId, String password, InvoiceDetails invoiceDetails) {
        return createInvoiceAndGetDownloadURL(userId, password, invoiceDetails, true);
    }

    public String createInvoiceAndGetDownloadURL(
            String userId, String password, InvoiceDetails invoiceDetails, boolean sign) {
        CreateInvoiceResult result = createInvoice(userId, password, invoiceDetails, sign);
        return getDownloadURL(result.token(), result.uuid(), result.signed());
    }

    public String createInvoiceAndGetHTML(String userId, String password, InvoiceDetails invoiceDetails) {
        return createInvoiceAndGetHTML(userId, password, invoiceDetails, true);
    }

    public String createInvoiceAndGetHTML(String userId, String password, InvoiceDetails invoiceDetails, boolean sign) {
        CreateInvoiceResult result = createInvoice(userId, password, invoiceDetails, sign);
        return getInvoiceHTML(result.token(), result.uuid(), result.signed());
    }
}
