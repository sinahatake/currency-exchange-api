package org.example.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ExchangeApiIntegrationTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8080/currency-exchange";
    }

    @Test
    @DisplayName("GET /exchange - Сценарий 1: Прямой курс (например, USD -> EUR)")
    public void exchange_DirectRate_Success() {
        given()
                .queryParam("from", "USD")
                .queryParam("to", "EUR")
                .queryParam("amount", "100")
                .when()
                .get("/exchange")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("baseCurrency.code", equalTo("USD"))
                .body("targetCurrency.code", equalTo("EUR"))
                .body("rate", notNullValue())
                .body("convertedAmount", notNullValue());
    }

    @Test
    @DisplayName("GET /exchange - Сценарий 2: Обратный курс (например, EUR -> USD)")
    public void exchange_ReverseRate_Success() {
        // Если в базе есть USD-EUR (0.9), то EUR-USD должен рассчитаться как 1/0.9
        given()
                .queryParam("from", "EUR")
                .queryParam("to", "USD")
                .queryParam("amount", "10")
                .when()
                .get("/exchange")
                .then()
                .statusCode(200)
                .body("rate", notNullValue());
    }

    @Test
    @DisplayName("GET /exchange - Сценарий 3: Кросс-курс через USD (например, AUD -> RUB)")
    public void exchange_CrossRate_Success() {
        // У тебя в базе есть USD-AUD и USD-RUB
        given()
                .queryParam("from", "AUD")
                .queryParam("to", "RUB")
                .queryParam("amount", "10")
                .when()
                .get("/exchange")
                .then()
                .statusCode(200)
                .body("baseCurrency.code", equalTo("AUD"))
                .body("targetCurrency.code", equalTo("RUB"));
    }

    @Test
    @DisplayName("GET /exchange - Ошибка 404: Курс не найден")
    public void exchange_NotFound() {
        given()
                .queryParam("from", "JPY") // Допустим, JPY нет в базе
                .queryParam("to", "RUB")
                .queryParam("amount", "10")
                .when()
                .get("/exchange")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /exchange - Ошибка 400: Некорректные параметры")
    public void exchange_InvalidParams() {
        given()
                .queryParam("from", "US") // Код меньше 3 символов
                .queryParam("to", "RUB")
                .queryParam("amount", "10")
                .when()
                .get("/exchange")
                .then()
                .statusCode(400);
    }
}