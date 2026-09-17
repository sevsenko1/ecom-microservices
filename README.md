# E-Commerce Microservices

Spring Boot tabanli e-ticaret mikroservis projesi. Proje; merkezi konfigurasyon, servis kesfi, API gateway, kullanici, urun ve siparis servislerinden olusur.

## Teknolojiler

- Java 21
- Spring Boot 3.4.x
- Spring Cloud Config Server
- Netflix Eureka
- Spring Cloud Gateway
- Spring Web / Spring Data JPA / Spring Data MongoDB
- PostgreSQL
- MongoDB
- RabbitMQ ve Spring Cloud Bus
- Actuator, Micrometer, Prometheus ve Zipkin tracing
- Maven Wrapper

## Servisler

| Servis | Dizin | Port | Gorev |
| --- | --- | ---: | --- |
| Config Server | `configserver/configserver` | `8888` | Servis konfigurasyonlarini merkezi olarak yayinlar. |
| Eureka Server | `eureka/eureka` | `8761` | Servis kesfi ve registry yonetimi saglar. |
| Gateway | `gateway/gateway` | `8080` | Dis istekler icin API gateway katmani. |
| Product Service | `product/product` | `8081` | Urun CRUD ve arama islemleri. |
| User Service | `user/user` | `8082` | Kullanici ve adres islemleri. |
| Order Service | `order/order` | `8083` | Sepet ve siparis islemleri. |

> Not: Gateway route tanimlari `configserver/configserver/src/main/resources/config/gateway-service.yml` icinde hazir bulunuyor ancak su anda yorum satiri olarak kapali.

## Proje Yapisi

```text
ecom-microservices/
|-- configserver/
|-- eureka/
|-- gateway/
|-- product/
|-- user/
|-- order/
|-- additionals/
|   |-- evaluate-loki/
|   `-- evaluate-prometheus/
`-- docker-compose.yml
```

## Gereksinimler

- JDK 21
- Docker Desktop
- MongoDB
- Maven kullanmak zorunlu degil; her serviste Maven Wrapper (`mvnw.cmd`) bulunur.

## Altyapiyi Baslatma

Kok dizinde PostgreSQL, pgAdmin ve RabbitMQ icin:

```powershell
docker compose up -d
```

Erisim adresleri:

- pgAdmin: `http://localhost:5050`
- RabbitMQ Management UI: `http://localhost:15672`
- RabbitMQ default kullanici: `guest`
- RabbitMQ default sifre: `guest`

`docker-compose.yml` PostgreSQL icin varsayilan kullanici/sifre olarak `sevko123/sevko123` kullanir.

Product ve Order servisleri PostgreSQL'de ayri veritabani bekler:

```powershell
docker exec -it postgres_container psql -U sevko123 -c "CREATE DATABASE product;"
docker exec -it postgres_container psql -U sevko123 -c "CREATE DATABASE ""order"";"
```

User servisi MongoDB bekler. MongoDB local olarak `mongodb://localhost:27017/ecom-user` adresinde calismalidir.

## Ortam Degiskenleri

Product ve Order servisleri:

```env
DB_USER=sevko123
DB_PASSWORD=sevko123
```

User servisi:

```env
MONGO_URI=mongodb://localhost:27017/ecom-user
```

Bu degerler servis calistirilirken terminal environment'i, IDE run configuration'i veya `.env` dosyalari uzerinden verilebilir.

## Servisleri Calistirma

Onerilen baslatma sirasi:

1. PostgreSQL, RabbitMQ ve MongoDB
2. Config Server
3. Eureka Server
4. Product Service ve User Service
5. Order Service
6. Gateway

Her servis kendi Maven projesi oldugu icin ilgili dizinden calistirilir.

```powershell
cd configserver\configserver
.\mvnw.cmd spring-boot:run
```

```powershell
cd eureka\eureka
.\mvnw.cmd spring-boot:run
```

```powershell
cd product\product
.\mvnw.cmd spring-boot:run
```

```powershell
cd user\user
.\mvnw.cmd spring-boot:run
```

```powershell
cd order\order
.\mvnw.cmd spring-boot:run
```

```powershell
cd gateway\gateway
.\mvnw.cmd spring-boot:run
```

## Konfigurasyon

Servisler temel ayarlarini local `application.yml` dosyalarindan alir ve ek konfigurasyon icin Config Server'a baglanir:

```yaml
spring:
  config:
    import: optional:configserver:http://localhost:8888
```

Config Server native profile ile calisir ve config dosyalarini su dizinden okur:

```text
configserver/configserver/src/main/resources/config
```

Config Server uzerinden kontrol edilebilecek ornek URL'ler:

- `http://localhost:8888/product-service/default`
- `http://localhost:8888/user-service/default`
- `http://localhost:8888/order-service/default`
- `http://localhost:8888/gateway-service/default`

## API Endpointleri

Product Service (`http://localhost:8081`):

| Method | Endpoint | Aciklama |
| --- | --- | --- |
| `POST` | `/api/products` | Urun olusturur. |
| `GET` | `/api/products` | Tum urunleri listeler. |
| `GET` | `/api/products/{id}` | ID'ye gore urun getirir. |
| `PUT` | `/api/products/{id}` | Urun gunceller. |
| `DELETE` | `/api/products/{id}` | Urun siler. |
| `GET` | `/api/products/search?keyword=...` | Urun arar. |

User Service (`http://localhost:8082`):

| Method | Endpoint | Aciklama |
| --- | --- | --- |
| `GET` | `/api/users` | Tum kullanicilari listeler. |
| `GET` | `/api/users/{id}` | ID'ye gore kullanici getirir. |
| `POST` | `/api/users` | Kullanici olusturur. |
| `PUT` | `/api/users/{id}` | Kullanici gunceller. |

Order Service (`http://localhost:8083`):

| Method | Endpoint | Header | Aciklama |
| --- | --- | --- | --- |
| `POST` | `/api/cart` | `X-User-ID` | Sepete urun ekler. |
| `GET` | `/api/cart` | `X-User-ID` | Kullanicinin sepetini getirir. |
| `DELETE` | `/api/cart/items/{productId}` | `X-User-ID` | Sepetten urun siler. |
| `POST` | `/api/orders` | `X-User-ID` | Sepetten siparis olusturur. |

Order servisi urun ve kullanici bilgilerini Eureka uzerinden `product-service` ve `user-service` servis adlariyla cagirir.

## Actuator ve Gozlemlenebilirlik

Servislerde actuator endpointleri genis sekilde aciktir:

```text
/actuator
/actuator/health
/actuator/prometheus
```

Loglar servis dizinleri altinda `logs/${spring.application.name}.log` formatinda tutulur.

Ek gozlemlenebilirlik denemeleri icin:

- `additionals/evaluate-prometheus`
- `additionals/evaluate-loki`

## Test

Her servis icin testler ilgili servis dizininden calistirilabilir:

```powershell
.\mvnw.cmd test
```

Ornek:

```powershell
cd product\product
.\mvnw.cmd test
```

## Sik Karsilasilan Durumlar

- Config Server calismadan diger servisler kendi remote config ayarlarini alamaz.
- Eureka calismadan servis kesfi ve Order servisinin service-name ile yaptigi cagrilar basarisiz olabilir.
- PostgreSQL icinde `product` ve `order` veritabanlari yoksa Product veya Order servisi acilista hata verebilir.
- User servisi icin MongoDB localde calismiyorsa uygulama Mongo baglanti hatasi alir.
- Gateway route'lari yorum satirinda oldugu icin dis trafik yonlendirmesi bekleniyorsa `gateway-service.yml` icindeki route tanimlari aktif hale getirilmelidir.
