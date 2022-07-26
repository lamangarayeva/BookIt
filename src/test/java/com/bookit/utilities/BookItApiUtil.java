package com.bookit.utilities;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class BookItApiUtil {

    public static String generateToken(String email, String password){

        Response response = given().accept(ContentType.JSON)
                .queryParams("email", email, "password", password)
                .when().get(Environment.BASE_URL + "/sign");

        String token = response.path("accessToken");

        return "Bearer "+token;
    }

    // one method param --> userType "student-member", "student-leader", "teacher"
    // returns --> token as String
    public static String getTokenByRole(String role){

        Response response;
        String email, password;

        switch (role) {

            case "student-member":
                email = Environment.MEMBER_EMAIL;
                password = Environment.MEMBER_PASSWORD;
                break;

            case "student-leader":
                email = Environment.LEADER_EMAIL;
                password = Environment.LEADER_PASSWORD;
                break;

            case "teacher":
                email = Environment.TEACHER_EMAIL;
                password = Environment.TEACHER_PASSWORD;
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + role);
        }

        response = given().accept(ContentType.JSON)
                .queryParams("email", email, "password", password)
                .get(Environment.BASE_URL + "/sign");

        String token = response.path("accessToken");

        System.out.println(role+" : "+token);
        return "Bearer " + token;
    }

    public static void deleteStudent(String studentEmail, String studentPassword){
        //we need id of new student to delete as a teacher
        //how to get id ?
        //1.login api using new student email and password
        //2.send get request to /api/users/me endpoint with new student token
        //3.get the id of the student
        //4.use that id for delete student as a teacher

        // 1. send GET request to get token with student information
        String studentToken = BookItApiUtil.generateToken(studentEmail, studentPassword);

        // 2. send GET request to /api/users/me endpoint and get the id number
        int idToDelete = given().accept(ContentType.JSON)
                .and().header("Authorization", studentToken)
                .when().get(Environment.BASE_URL + "/api/users/me")
                .then().statusCode(200).extract().jsonPath().getInt("id");

        // 3. delete request as a teacher to /api/students/{id} endpoint to delete the student
        String teacherToken = BookItApiUtil.generateToken(Environment.TEACHER_EMAIL, Environment.TEACHER_PASSWORD);

        given().pathParam("id", idToDelete)
                .and().header("Authorization", teacherToken)
                .when().delete(Environment.BASE_URL +"/api/students/{id}")
                .then().statusCode(204);
    }

}
