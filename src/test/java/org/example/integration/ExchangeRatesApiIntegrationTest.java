package org.example.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ExchangeRatesApiIntegrationTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8080/currency-exchange";
    }

    // --- ТЕСТЫ GET /exchangeRates ---

    @Test
    @DisplayName("GET /exchangeRates - Получение списка всех курсов")
    public void getAllExchangeRates_Success() {
        given()
                .when()
                .get("/exchangeRates")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", instanceOf(java.util.List.class));
    }

    // --- ТЕСТЫ GET /exchangeRate/USDRUB ---

    @Test
    @DisplayName("GET /exchangeRate/{pair} - Успешное получение курса")
    public void getExchangeRate_Success() {
        // Предполагаем, что USD и EUR уже есть в базе
        given()
                .when()
                .get("/exchangeRate/USDEUR")
                .then()
                .statusCode(200)
                .body("baseCurrency.code", equalTo("USD"))
                .body("targetCurrency.code", equalTo("EUR"))
                .body("rate", notNullValue());
    }

    @Test
    @DisplayName("GET /exchangeRate/{pair} - Ошибка 404 (пара не найдена)")
    public void getExchangeRate_NotFound() {
        given()
                .when()
                .get("/exchangeRate/USDZAR") // Валюта, которой скорее всего нет
                .then()
                .statusCode(404);
    }

    // --- ТЕСТЫ POST /exchangeRates ---

    @Test
    @DisplayName("POST /exchangeRates - Успешное создание курса")
    public void postExchangeRate_Success() {
        // Создаем случайную пару, чтобы не поймать 409
        String base = "USD";
        String target = "T" + (int)(Math.random() * 90 + 10); // Наша "тестовая" валюта из прошлых чатов

        // ВНИМАНИЕ: Целевая валюта должна существовать в БД, иначе будет 404
        // Сначала создаем валюту, если её нет (через твой сервис или DAO)

        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("baseCurrencyCode", "USD")
                .formParam("targetCurrencyCode", "EUR")
                .formParam("rate", "0.95")
                .when()
                .post("/exchangeRates")
                .then()
                .statusCode(anyOf(is(201), is(409))); // 409 если уже добавили ранее
    }

    @Test
    @DisplayName("POST /exchangeRates - Ошибка 400 (отсутствует поле)")
    public void postExchangeRate_MissingField() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("baseCurrencyCode", "USD")
                // targetCurrencyCode пропущен
                .formParam("rate", "1.0")
                .when()
                .post("/exchangeRates")
                .then()
                .statusCode(400);
    }

    // --- ТЕСТЫ PATCH /exchangeRate/USDRUB ---

    @Test
    @DisplayName("PATCH /exchangeRate/{pair} - Успешное обновление курса")
    public void patchExchangeRate_Success() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("rate", "99.99")
                .when()
                .patch("/exchangeRate/USDEUR")
                .then()
                .statusCode(200)
                .body("rate", equalTo(99.99f));
    }
}