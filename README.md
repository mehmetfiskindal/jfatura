# 🧾 JFatura

GİB e-Arşiv sistemi üzerinde fatura oluşturmanızı sağlayan **Java 25 + Spring Boot 4** starter'ı.
([Fatura.js](https://github.com/f/fatura) TypeScript paketinin birebir Java portu; aynı özellikler, aynı testler.)

### Alternatifler

| Dil | Repo | Geliştirici |
| --- | --- | --- |
| TS/JS | https://github.com/f/fatura | Fatih Kadir Akın |
| PHP | https://github.com/AdemAliDurmus/fatura | Adem Ali Durmuş |
| PHP | https://github.com/furkankadioglu/efatura | Furkan Kadıoğlu |
| PHP | https://github.com/mlevent/fatura | Mert Levent |
| C# | https://github.com/BFYDigital/e-arsiv-fatura-dotnet | BFY Digital |

> Bu sistem **https://earsivportal.efatura.gov.tr/** adresini kullanarak bu sistem üzerinden fatura oluşturmanızı sağlar.

> Bu sistem GİB'e tabi **şahıs şirketi** ya da **şirket** hesapları ile çalışır ve bu kişilikler adına resmi fatura oluşturur. Kesilen faturaları https://earsivportal.efatura.gov.tr/ adresinden görüntüleyebilir ya da bu kütüphane ile indirebilirsiniz.

#### Kullanıcı Adı ve Parola Bilgileri

> [https://earsivportal.efatura.gov.tr/intragiris.html](https://earsivportal.efatura.gov.tr/intragiris.html) adresindeki parola ekranında kullanılan kullanıcı kodu ve parola ile bu paketi kullanabilirsiniz.
> ℹ️ Bu **kullanıcı kodu ve parola bilgilerini** muhasebecinizden ya da **GİB - İnteraktif Vergi Dairesi**'nden edinebilirsiniz.

## Yükleme

**Maven:**

```xml
<dependency>
    <groupId>io.github.mehmetfiskindal</groupId>
    <artifactId>jfatura-spring-boot-starter</artifactId>
    <version>0.2.1</version>
</dependency>
```

**Gradle:**

```groovy
implementation 'io.github.mehmetfiskindal:jfatura-spring-boot-starter:0.2.1'
```

**Java 25 veya üzeri** gereklidir.

## Kullanım

Tüm işlemler bir **`FaturaClient`** örneği üzerinden yapılır.

Spring Boot uygulamasında bean otomatik gelir:

```java
@Service
public class FaturaServisi {
    private final FaturaClient client;

    public FaturaServisi(FaturaClient client) {
        this.client = client;
    }
}
```

```yaml
# application.yml — üretim için varsayılan zaten PROD'dur
jfatura:
  environment: PROD        # PROD (varsayılan) | TEST
```

Test ortamı (`https://earsivportaltest.efatura.gov.tr`) ya da Spring dışı kullanım:

```java
FaturaClient client = FaturaClient.create(Environment.TEST);
// veya
FaturaClient client = new FaturaClient(); // varsayılan: PROD
```

Oldukça kolay bir kullanıma sahiptir:

#### `createInvoiceAndGetDownloadURL(user, pass, invoice, sign)`

Bu method ile fatura oluşturulup imzalanır ve indirme adresi döner.

```java
FaturaClient client = FaturaClient.create();

String faturaURL = client.createInvoiceAndGetDownloadURL(
        "GIB Kullanıcı Adı",
        "GIB Parolası",
        InvoiceDetails.builder("08/02/2020", "09:07:48")
                .taxIDOrTRID("11111111111")
                .title("FATIH AKIN")
                .fullAddress("X Sok. Y Cad. No: 3 Z Istanbul")
                .items(List.of(InvoiceItem.builder()
                        .name("Stickker")
                        .quantity(1)
                        .unitPrice(100.0)
                        .price(100)
                        .vatRate(20.0)
                        .vatAmount(20.0)
                        .build()))
                .grandTotal(100)
                .totalVAT(20)
                .grandTotalInclVAT(120)
                .paymentTotal(120)
                .build(),
        // Varsayılan olarak imzalı (true) gönderilir.
        false);
```

#### `createInvoiceAndGetHTML(user, pass, invoice, sign)`

Bu method ile fatura oluşturulup imzalanır ve fatura HTML'i döner. Bu HTML'i `iframe` içerisinde gösterip yazdırılmasını sağlayabilirsiniz.

```java
String faturaHTML = client.createInvoiceAndGetHTML(user, pass, details, false);
```

---

## Diğer metodlar (`client` üzerinde)

Aşağıdakilerin tümü `FaturaClient` örneği üzerinde çağrılır.

#### `getToken(userName, password)`

**e-Arşiv Portal** oturumu için `token` döner.

#### `createDraftInvoice(token, invoiceDetails)`

e-Arşiv'de önce **taslak** oluşturulur. Dönüşte `uuid`, `date`, `documentNumber`, `listItem` ve ham GİB cevabı bulunur.

> ℹ️ GİB ETTN'i sunucu tarafında atar; `InvoiceDetails.uuid` alanı GİB'e gönderilmez. Yeni kayıt, o günün taslak listesi karşılaştırılarak bulunur.

#### `findInvoice(token, draftInvoice)`

`createDraftInvoice`'ın döndürdüğü taslak nesnesi verilir; ilgili gündeki taslaklar arasından eşleşen kayıt `Optional<InvoiceListItem>` olarak döner. **İmzalama** için gereken bilgiler (ör. belge numarası) bu nesnede yer alır.

#### `getAllInvoicesByDateRange(token, DateRange)` / `getAllInvoicesIssuedToMeByDateRange(token, DateRange)`

İki tarih arasındaki **kesilen/taslak** listesi ve **adıma düzenlenen** belgeler.

#### `signDraftInvoice(token, listItem)` ☢️

İmzalama **kesilmiş sayılan mali işlem** oluşturur; dikkatli kullanın. HSM cihazı ile imzalar.

#### `getDownloadURL(token, uuid, signed)` / `getInvoiceHTML(token, uuid, signed)`

İndirme URL'si (`.zip` içinde HTML/XML olabilir) ve fatura HTML metni. `signed`: fatura onaylı mı (`true`/`false`).

#### `cancelDraftInvoice(token, reason, listItem)`

Taslak iptali.

#### `getRecipientDataByTaxIDOrTRID(token, taxIDOrTRID)`

VKN/TCKN ile alıcı bilgisi sorgusu.

#### `sendSignSMSCode` / `verifySignSMSCode` / `getSignPhoneNumber`

İmzaya ilişkin SMS doğrulama akışı (GİB kurallarına tabi). `verifySignSMSCode` kodu doğrular **ve** verilen faturaları imzalar.

#### `getUserData(token)` / `updateUserData(token, userData)`

Portal kullanıcı bilgilerini okuma / güncelleme.

#### `createInvoice(user, pass, invoiceDetails[, sign])`

Token alır, taslak oluşturur, bulur; `sign=true` (varsayılan) ise imzalar. `findInvoice` sonuç vermezse imza atlanır.

#### `logout(token)`

Oturum kapatma.

## Spring Boot yapılandırması

| Özellik | Varsayılan | Açıklama |
| --- | --- | --- |
| `jfatura.environment` | `PROD` | `PROD` veya `TEST` ortamı |
| `jfatura.base-url` | — | Ortam URL'sini geçersiz kılmak için (opsiyonel) |

## Geliştirme

```bash
./mvnw test          # 247 unit testi çalıştırır
./mvnw verify        # unit + JaCoCo (%80 eşik) + integration testleri
GIB_TEST_USER=... GIB_TEST_PASS=... ./mvnw verify   # canlı GİB TEST hesabıyla integration
./mvnw failsafe:integration-test    # yalnızca integration testleri
```

Integration testleri gerçek **GİB TEST portalına** bağlanır (`earsivportaltest.efatura.gov.tr`); giriş başarısız olursa veya `CI=true` ise otomatik atlanır. Kamuya açık test hesabı: `33333301` / `1`.

## Lisans

MIT

---

> ☢️ **BU PAKET VERGİYE TABİ OLAN MALİ VERİ OLUŞTURUR.** BU PAKET NEDENİYLE OLUŞABİLECEK SORUNLARDAN BU PAKET SORUMLU TUTULAMAZ, RİSK KULLANANA AİTTİR. RİSKLİ GÖRÜYORSANIZ KULLANMAYINIZ.
