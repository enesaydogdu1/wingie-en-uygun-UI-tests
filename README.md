# ✈️ Wingie EnUygun Group / Case Study

## 🚀 Genel Bakış  

Proje, **Java + Selenium + TestNG + Maven** altyapısı üzerinde çalışıyor.  
Test sonuçları **Allure Reports** ile raporlanıyor, loglama işlemleri ise **Log4j2** aracılığıyla gerçekleştiriliyor.  

Parametrelendirme seçeneği ise config.properties içerisinde yer alıyor. Kalkış saatleri, varış şehri, dönüş şehri ve browser gibi parametreleri config.parameters üzerinden yönetebilirsiniz. 

---

## 🧩 Proje Mimarisi  

Projeyi POM Design'a uygun şekilde ilerletmeyi hedefledim. 
Ana klasör yapısı şu şekilde:

📂 **src**  
├── 📂 **main**  
│   ├── 📂 **java**  
│   │   ├── 📁 analysis  
│   │   ├── 📁 base  
│   │   ├── 📁 listeners  
│   │   ├── 📁 locator  
│   │   ├── 📁 pages  
│   │   └── 📁 utils  
│   └── 📂 **resources**  
│       ├── ⚙️ config.properties  
│       └── ⚙️ log4j2.xml  
├── 📂 **test**  
│   └── 📂 **java**  
│       ├── 📂 **tests**  
│       │   ├── 🧪 TC1_FlightSearchTest  
│       │   ├── 🧪 TC2_PriceSortByAirlineTest  
│       │   ├── 🧪 TC3_CriticalPathTest  
│       │   └── 🧪 TC4_FlightDataExtractionAndAnalysisTest  
│       └── 📂 **utils**  
│           └── 🧩 AssertionsHelper  
├── 📂 **target**  
│   ├── 📁 allure-results  
│   ├── 📂 **analysis**  
│   │   └── 📂 **Istanbul_Lefkosa_2025-10-29**  
│   │       ├── 🖼️ heatmap_price_by_airline_timeslot.png  
│   │       ├── 🖼️ min_max_avg_by_airline.png  
│   │       ├── 📊 summary_stats.csv  
│   │       └── 📊 top_cost_effective.csv  
│   ├── 📂 **exports**  
│   │   └── 📊 flights_Istanbul_Lefkosa.csv  
└── 📄 test.log

---

### 🧠 Katman Özeti

| 📁 Katman | 🧩 Açıklama | 📄 Örnek İçerik / Sınıflar |
| :--- | :--- | :--- |
| **analysis** | Uçuş verilerinin analizi, istatistiksel işlemler ve grafik üretimi. | `AnalysisRunner`, `ChartService`, `PriceAggregator` |
| **base** | Test altyapısının temel sınıfları (Page Object Model temel sınıfı). | `BasePage`, `BaseTest` |
| **listeners** | TestNG olaylarını yakalayan ve raporlama entegrasyonunu sağlayan sınıflar. | `TestListener` |
| **locator** | Sayfa elementlerinin locator bilgilerini içeren sınıflar. | `HomePageLocator`, `ResultsPageLocator` |
| **pages** | Page Object Model yapısındaki sayfa etkileşim sınıfları. | `HomePage`, `ResultsPage` |
| **utils** | Yardımcı fonksiyonlar, konfigürasyon ve sürücü yönetimi. | `ConfigReader`, `DriverFactory` |
| **resources** | Yapılandırma ve log ayar dosyaları. | `config.properties`, `log4j2.xml` |
| **tests** | Test senaryolarının bulunduğu ana katman. | `TC1_FlightSearchTest`, `TC4_FlightDataExtractionAndAnalysisTest` |
| **target/analysis** | Analiz sonuçları, grafikler ve CSV raporları. | `heatmap_price_by_airline_timeslot.png`, `summary_stats.csv` |

---

## 🧪 Test Senaryoları 

### 1️⃣ Uçuş Arama Testi (TC1: Flight Search Test)
**Amaç:** Uçuş arama fonksiyonunun zaman filtresiyle birlikte doğrulanması.

| Adım | Açıklama |
| :--- | :--- |
| 1 | `www.enuygun.com` adresine gidilir. |
| 2 | Parametrik olarak alınan gidiş–dönüş (İstanbul ↔ Ankara örneği) uçuş araması yapılır. |
| 3 | Sonuç sayfasında **10:00–18:00** kalkış saat filtresi uygulanır. |
| 4 | Görüntülenen tüm uçuşların bu zaman aralığında olduğu ve rota bilgilerinin doğruluğu kontrol edilir. |

### 2️⃣ Havayolu Bazında Fiyat Sıralama Testi (TC2: Price Sort by Airline Test)
**Amaç:** Belirli bir havayolu için fiyat sıralamasının artan düzende çalıştığını doğrulamak.

| Adım | Açıklama |
| :--- | :--- |
| 1 | İstanbul ↔ Ankara araması yapılır ve 10:00–18:00 filtresi uygulanır. |
| 2 | Yalnızca **Türk Hava Yolları** uçuşları seçilir. |
| 3 | Fiyat sıralaması “**artan**” olarak ayarlanır. |
| 4 | Tüm sonuçların Türk Hava Yolları'na ait olduğu ve fiyatların artan düzende listelendiği doğrulanır. |

### 3️⃣ Kritik Yol Testi (TC3: Critical Path Test)
**Amaç:** Kullanıcının temel akışını (uçuş seçimi → ödeme sayfası) doğrulamak.

| Adım | Açıklama |
| :--- | :--- |
| 1 | Bir uçuş araması yapılır ve bir uçuş seçilir. |
| 2 | Yolcu bilgileri doldurulur ve ödeme sayfasına ilerlenir. |
| 3 | Ödeme sayfasına başarılı bir şekilde yönlendirildiğinin doğrulaması yapılır. |

### 4️⃣ Uçuş Verisi Çıkarma ve Analiz Testi (TC4: Flight Data Extraction and Analysis Test)
**Amaç:** Uçuş verilerini dışa aktarma, analiz etme ve görselleştirmek.

| Adım | Açıklama |
| :--- | :--- |
| 1 | İstanbul → Lefkoşa uçuşu için arama yapılır. |
| 2 | Tüm uçuş sonuçları (saat, havayolu, fiyat, süre vb.) CSV dosyasına kaydedilir (`/target/exports/`). |
| 3 | Python tabanlı analiz kodu çalıştırılarak istatistikler ve grafikler üretilir. |
| 4 | Çıktı dosyalarının (CSV ve PNG) doğru şekilde üretildiği kontrol edilir. |
| 5 | Alınan çıktılara göre en ucuz maliyet hesabı yapılır. |

**Çıktı Örnekleri:**
* `/target/exports/flights_Istanbul_Lefkosa.csv`
* `/target/analysis/summary_stats.csv` (Min/Max/Ortalama Fiyatlar)
* `/target/analysis/heatmap_price_by_airline_timeslot.png`
* `/target/analysis/min_max_avg_by_airline.png`
---

## 📊 Raporlama ve Loglama

### Allure Raporları
Tüm testler sonunda otomatik olarak detaylı **Allure Report** üretilir ve bu rapor şunları içerir:  
* Testin süresi.
* Geçen/kalan adımlar (step'ler).
* Hata durumunda otomatik olarak alınan **ekran görüntüleri (screenshot)**.
* Test adımlarının açıklamaları.

<img width="1916" height="971" alt="Image of Allure Report Dashboard" src="https://github.com/user-attachments/assets/39af053e-75f8-405c-8101-d8d7ccf50833" />  
<img width="1919" height="992" alt="allureReportsTestResult" src="https://github.com/user-attachments/assets/fa3db431-663b-461b-b9cf-e3df3d3cf451" />  
<img width="1919" height="378" alt="allureTestTime" src="https://github.com/user-attachments/assets/60c17bce-fc3f-4872-9ad4-656cdb36c52a" />  

### Loglama (Log4j2)
Testlerin her adımı `test.log` dosyasına yazılır. Bu, hata ayıklama sürecini hızlandırır ve testin akışını adım adım izlemeyi sağlar.

<img width="1316" height="489" alt="Image of Log4j2 Output" src="https://github.com/user-attachments/assets/4b656ae1-320b-4278-ae34-e3a661da1c07" />

---

## 💾 İş Analitiği Çıktısı (Veri Analizi)

Testler, sadece doğrulama yapmakla kalmayıp, elde edilen uçuş verilerini kullanarak iş analizi çıktıları da üretmektedir.

### Görselleştirme Örnekleri

* **Ortalama Fiyatlara Göre Isı Haritası**
    <img width="1100" height="650" alt="heatmap_price_by_airline_timeslot" src="https://github.com/user-attachments/assets/9c47393e-4828-4e66-9a75-209ec339af5a" />

* **Havayolu Bazlı Min/Ort/Max Fiyat Karşılaştırması**
    <img width="1100" height="650" alt="min_max_avg_by_airline" src="https://github.com/user-attachments/assets/705cff82-1364-4e90-925f-f6ca93bcb816" />

### 💰 En Uygun Maliyet Analizi

Uçuş için en uygun maliyet hesaplama algoritması geliştirilmiştir. Bu algoritma, fiyat, süre ve bağlantı sayısını dikkate alarak bir skor üretir ve en düşük skora sahip uçuş, en uygun maliyetli olarak belirlenir.

$score = \text{price} + \alpha \cdot \text{durationMin} + \beta \cdot \text{stopsCount} - \gamma \cdot (\text{isDirect} ? 1 : 0)$

Sonuçlar `/target/analysis/top_cost_effective.csv` dosyasına kaydedilir.

<img width="591" height="346" alt="cost" src="https://github.com/user-attachments/assets/9326f155-9cf3-4641-929d-51c2df8dc970" />

---

## 💻 Kurulum ve Çalıştırma (Installation & Run)

### Projeyi Klonla
```
$ git clone https://github.com/enesaydogdu1/wingie-en-uygun-UI-tests
$ cd wingie-en-uygun-UI-tests
```

### Maven Bağımlılıklarını Yükleme
```
$ mvn clean install
```

### Testleri Çalıştırma ve Sonuçları Allure Reports üzerinden görüntüleme
```
$ mvn clean test
$ mvn allure:serve
```

## 🧩 Tarayıcı Sürücüsü Hakkında (Driver Note)

Tarayıcı seçimi, `config.properties` dosyası üzerinden `browser=chrome` veya `browser=firefox` şeklinde yapılabilmektedir.

Proje **Chrome** tarayıcısında tamamen stabil çalışmaktadır. Ancak **Firefox** seçildiğinde bazı testler, element senkronizasyonu sorunları nedeniyle kırılabiliyor.

> Bu durumun büyük olasılıkla **WebDriverWait davranış farklarından** kaynaklandığını düşünüyorum. Ancak iyi yönünden bakmak gerekirse, bu sayede başarısız olan test case'leri de Allure Reports üzerinde görüntüleyip screenshot mekanizmasının çalıştığını doğrulamış oldum :)

---

## 💬 Yazar Notu

Son olarak incelemeniz için teşekkür ederim, ismim **Enes**. Mevcut GitHub hesabıma 2FA sebebiyle erişemediğim için yeni bir GitHub hesabı oluşturdum, önceki çalışmalarıma eski hesabım üzerinden ulaşabilirsiniz:
[https://github.com/swenes](https://github.com/swenes)

