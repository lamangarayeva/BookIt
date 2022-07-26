package com.bookit.utilities;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class BookItApiUtil {

    public static String generateToken(String email, String password){

        Response response = given().accept(ContentType.JSON)
                .queryParams("email", email, "password", password)
                .when().get(ConfigurationReader.get("base_url") + "/sign");

        String token = response.path("accessToken");

        return "Bearer "+token;
    }

    // one method param --> userType "student-member", "student-leader", "teacher"
    // returns --> token as String
    public static String getRole(String role){

        Response response;
        String email, password;

        switch (role) {

            case "student-member":
                email = ConfigurationReader.get("team_member_email");
                password = ConfigurationReader.get("team_member_password");
                break;

            case "student-leader":
                email = ConfigurationReader.get("team_leader_email");
                password = ConfigurationReader.get("team_leader_password");
                break;

            case "teacher":
                email = ConfigurationReader.get("teacher_email");
                password = ConfigurationReader.get("teacher_password");
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + role);
        }

        response = given().accept(ContentType.JSON)
                .queryParams("email", email, "password", password)
                .get(ConfigurationReader.get("base_url") + "/sign");

        String token = response.path("accessToken");

        return "Bearer " + token;
    }

}
