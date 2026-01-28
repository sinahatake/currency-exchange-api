package org.example.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class CurrenciesApiIntegrationTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8080/currency-exchange";
    }

    // --- ГРУППА: GET /currencies ---

    @Test
    @DisplayName("GET /currencies - Успех (200)")
    public void getAllCurrencies_Success() {
        when()
                .get("/currencies")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("code", hasItems("USD", "EUR"));
    }

    // --- ГРУППА: GET /currency/{code} ---

    @Test
    @DisplayName("GET /currency/EUR - Успех (200)")
    public void getCurrencyByCode_Success() {
        when()
                .get("/currency/EUR")
                .then()
                .statusCode(200)
                .body("code", equalTo("EUR"))
                .body("name", equalTo("Euro"));
    }

    @Test
    @DisplayName("GET /currency/ - Отсутствует код (400)")
    public void getCurrencyByCode_NoCode() {
        when()
                .get("/currency/")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /currency/ZZZ - Не найдено (404)")
    public void getCurrencyByCode_NotFound() {
        when()
                .get("/currency/ZZZ")
                .then()
                .statusCode(404);
    }

    // --- ГРУППА: POST /currencies ---

    @Test
    @DisplayName("POST /currencies - Успех (201)")
    public void postCurrency_Success() {
        String uniqueCode = "T" + (int)(Math.random() * 90 + 10);
        given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .formParam("name", "Test Coin")
                .formParam("code", uniqueCode)
                .formParam("sign", "TC")
                .when()
                .post("/currencies")
                .then()
                .statusCode(201)
                .body("code", equalTo(uniqueCode))
                .body("id", notNullValue());
    }

    @Test
    @DisplayName("POST /currencies - Пропуск поля (400)")
    public void postCurrency_InvalidParam() {
        given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .formParam("name", "No Code Coin")
                // code пропущен
                .formParam("sign", "NC")
                .when()
                .post("/currencies")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /currencies - Дубликат (409)")
    public void postCurrency_AlreadyExists() {
        given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .formParam("name", "United States dollar")
                .formParam("code", "USD")
                .formParam("sign", "$")
                .when()
                .post("/currencies")
                .then()
                .statusCode(409);
    }
}