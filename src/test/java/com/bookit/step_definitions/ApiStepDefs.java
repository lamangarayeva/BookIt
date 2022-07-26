package com.bookit.step_definitions;

import com.bookit.pages.SelfPage;
import com.bookit.utilities.BookItApiUtil;
import com.bookit.utilities.ConfigurationReader;
import com.bookit.utilities.DBUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;
import org.openqa.selenium.WebElement;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiStepDefs {

    String token;
    Response response;
    String emailGlobal;

    @Given("I logged Bookit api using {string} and {string}")
    public void i_logged_Bookit_api_using_and(String email, String password) {

        token = BookItApiUtil.generateToken(email, password);
        emailGlobal = email;
    }

    @When("I get the current user information from api")
    public void i_get_the_current_user_information_from_api() {

        // send GET request "/api/users/me" endpoint to get current user info
        response = given().accept(ContentType.JSON)
                .and()
                .header("Authorization", token)
                .when().get(ConfigurationReader.get("base_url") + "/api/users/me");

    }

    @Then("status code should be {int}")
    public void status_code_should_be(int statusCode) {

        // no need to use Hamcrest Matchers, since cucumber framework and Junit assertions are used
        Assert.assertEquals(statusCode, response.statusCode());

    }

    @Then("the information about current user from api and database should match")
    public void the_information_about_current_user_from_api_and_database_should_match() {

        // get information from database
        String query = "select firstname, lastname, role from users\n" +
                "where email = '"+emailGlobal+"'";

        Map<String, Object> dbMap = DBUtils.getRowMap(query);
        //System.out.println("dbMap = " + dbMap);

        String expectedFirstName = (String) dbMap.get("firstname");
        String expectedLastName = (String) dbMap.get("lastname");
        String expectedRole = (String) dbMap.get("role");

        // get information from api
        JsonPath jsonPath = response.jsonPath();
        String actualFirstName = jsonPath.getString("firstName");
        String actualLastName = jsonPath.getString("lastName");
        String actualRole = jsonPath.getString("role");

        // compare database and api
        Assert.assertEquals(expectedFirstName, actualFirstName);
        Assert.assertEquals(expectedLastName, actualLastName);
        Assert.assertEquals(expectedRole, actualRole);

    }

    @Then("UI, API and Database user information must be match")
    public void ui_API_and_Database_user_information_must_be_match() {

        // get information from database
        String query = "select firstname, lastname, role from users\n" +
                "where email = '"+emailGlobal+"'";

        Map<String, Object> dbMap = DBUtils.getRowMap(query);
        //System.out.println("dbMap = " + dbMap);

        String expectedFirstName = (String) dbMap.get("firstname");
        String expectedLastName = (String) dbMap.get("lastname");
        String expectedRole = (String) dbMap.get("role");

        // get information from api
        JsonPath jsonPath = response.jsonPath();
        String actualFirstName = jsonPath.getString("firstName");
        String actualLastName = jsonPath.getString("lastName");
        String actualRole = jsonPath.getString("role");

        // get information from ui
        SelfPage selfPage = new SelfPage();
        String actualUIName = selfPage.name.getText();
        String actualUIRole = selfPage.role.getText();

        // UI vs DB
        String expectedFullName = expectedFirstName +" "+expectedLastName;
        // verify ui fullName and db fullName
        Assert.assertEquals(expectedFullName, actualUIName);
        Assert.assertEquals(expectedRole, actualRole);

        // UI vs API
        String actualFullName = actualFirstName+" "+actualLastName;

        Assert.assertEquals(actualFullName, actualUIName);
        Assert.assertEquals(actualRole, actualUIRole);

    }

    @When("I send POST request to {string} endpoint with following information")
    public void i_send_post_request_to_end_point_with_following_information(String endPoint, Map<String, String> studentInfo) {
        response = given().accept(ContentType.JSON)
                .queryParams(studentInfo)
                .header("Authorization", token)
                .log().all()
                .when().post(ConfigurationReader.get("base_url") + endPoint);

    }

}
