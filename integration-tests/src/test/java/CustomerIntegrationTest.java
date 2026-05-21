import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class CustomerIntegrationTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnCustomersWithValidToken() {
        /// Arrange
        String loginPayload = """
                {
                "email": "testuser@test.com",
                "password": "password123"
                }
                """;

        String token = given()
                .contentType("application/json")
                .body(loginPayload)
                /// Act
                .when()
                .post("auth/login")
                .then() /// Assert
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/customers")
                .then()
                .statusCode(200)
                .body("customers", notNullValue());
    }

}
