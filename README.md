# EnUygun UI Tests — Selenium + TestNG + Allure + Log4j2

## Çalıştırma
```bash
mvn clean test
```
Tarayıcı seçimi:
```bash
mvn clean test -Dbrowser=firefox -Dheadless=true
```

## Raporlar
```bash
mvn allure:serve
```

## Case'ler
- **Durum 1:** Round Trip + 10:00–18:00 filtresi (FlightSearchTest)
- **Durum 2:** THY filtrele + fiyata göre artan sıralama (PriceSortTHYTest)
- **Durum 3:** Kritik yol—seçim ve yolcu formuna kadar (CriticalPathTest)
- **Durum 4:** İstanbul–Lefkoşa araması, CSV export (DataExtractionTest) + `analysis/analyze_flights.py`

## Analiz (Python)
`analysis/analyze_flights.py` script'i CSV'yi okur, havayoluna göre min/avg/max fiyatları hesaplar, bar grafik ve saat-bazlı ısı haritası üretir, ayrıca en uygun maliyetli uçuşları seçer.
```bash
python analysis/analyze_flights.py --csv-dir target/exports
```
Çıktılar: `analysis/output/` klasörü.
