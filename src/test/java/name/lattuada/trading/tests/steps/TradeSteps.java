package name.lattuada.trading.tests.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import name.lattuada.trading.model.EOrderType;
import name.lattuada.trading.model.dto.OrderDTO;
import name.lattuada.trading.model.dto.SecurityDTO;
import name.lattuada.trading.model.dto.TradeDTO;
import name.lattuada.trading.model.dto.UserDTO;
import name.lattuada.trading.tests.CucumberTest;
import name.lattuada.trading.tests.utils.RestUtility;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TradeSteps {

    private static final Logger logger = LoggerFactory.getLogger(CucumberTest.class);
    private final RestUtility restUtility;
    private final Map<String, SecurityDTO> securityMap;
    private final Map<String, UserDTO> userMap;
    private OrderDTO buyOrder;
    private OrderDTO sellOrder;

    TradeSteps() {
        restUtility = new RestUtility();
        securityMap = new HashMap<>();
        userMap = new HashMap<>();
    }

    @Given("one security {string} and two users {string} and {string} exist")
    public void oneSecurityAndTwoUsers(String securityName, String userName1, String userName2) {
        logger.trace("Got securityName = \"{}\"; userName1 = \"{}\"; userName2 = \"{}\"",
                securityName, userName1, userName2);
        createSecurity(securityName);
        createUser(userName1);
        createUser(userName2);
    }

    @When("user {string} puts a {string} order for security {string} with a price of {double} and quantity of {long}")
    @And("user {string} puts a {string} order for security {string} with a price of {double} and a quantity of {long}")
    public void userPutAnOrder(String userName, String orderType, String securityName, Double price, Long quantity) {
        logger.trace("Got username = \"{}\"; orderType = \"{}\"; securityName = \"{}\"; price = \"{}\"; quantity = \"{}\"",
                userName, EOrderType.valueOf(orderType.toUpperCase(Locale.ROOT)), securityName, price, quantity);
        assertTrue(String.format("Unknown user \"%s\"", userName),
                userMap.containsKey(userName));
        assertTrue(String.format("Unknown security \"%s\"", securityName),
                securityMap.containsKey(securityName));
        createOrder(userName,
                EOrderType.valueOf(orderType.toUpperCase(Locale.ROOT)),
                securityName,
                price,
                quantity);
    }

    @Then("a trade occurs with the price of {double} and quantity of {long}")
    public void aTradeOccursWithThePriceOfAndQuantityOf(Double price, Long quantity) {
        logger.trace("Got price = \"{}\"; quantity = \"{}\"",
                price, quantity);
        TradeDTO trade = restUtility.get("api/trades/orderBuyId/" + buyOrder.getId().toString()
                        + "/orderSellId/" + sellOrder.getId().toString(),
                TradeDTO.class);
        assertEquals("Price not expected", trade.getPrice(), price);
        assertEquals("Quantity not expected", trade.getQuantity(), quantity);
    }

    @Then("no trades occur")
    public void noTradesOccur() {
        assertThatThrownBy(() -> restUtility.get("api/trades/orderBuyId/" + buyOrder.getId().toString()
                        + "/orderSellId/" + sellOrder.getId().toString(),
                TradeDTO.class)).isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    private void createUser(String userName) {
        try {
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(userName);
            userDTO.setPassword(RandomStringUtils.randomAlphanumeric(64));
            UserDTO userReturned = restUtility.post("api/users", userDTO, UserDTO.class);
            userMap.put(userName, userReturned);
            logger.info("User created: {}", userReturned);
        } catch (RestClientException e) {
            logger.error("Failed to create user {}: RestClientException error {}", userName, e.getMessage());
            fail("RestClientException error during user creation: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error when creating user {}: {}", userName, e.getMessage());
            fail("Unexpected error during user creation: " + e.getMessage());
        }
    }

    private void createSecurity(String securityName) {
        try {
            SecurityDTO securityDTO = new SecurityDTO();
            securityDTO.setName(securityName);
            SecurityDTO securityReturned = restUtility.post("api/securities", securityDTO, SecurityDTO.class);
            securityMap.put(securityName, securityReturned);
            logger.info("Security created: {}", securityReturned);
        } catch (RestClientException e) {
            logger.error("Failed to create security {}: RestClientException error {}", securityName, e.getMessage());
            fail("RestClientException error during security creation: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error when creating security {}: {}", securityName, e.getMessage());
            fail("Unexpected error during security creation: " + e.getMessage());
        }
    }

    private void createOrder(String userName, EOrderType orderType, String securityName, Double price, Long quantity) {
        try {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setUserId(userMap.get(userName).getId());
            orderDTO.setSecurityId(securityMap.get(securityName).getId());
            orderDTO.setType(orderType);
            orderDTO.setPrice(price);
            orderDTO.setQuantity(quantity);

            OrderDTO orderReturned = restUtility.post("api/orders", orderDTO, OrderDTO.class);
            if (orderType == EOrderType.BUY) {
                buyOrder = orderReturned;
            } else if (orderType == EOrderType.SELL) {
                sellOrder = orderReturned;
            }
            logger.info("Order created: {}", orderReturned);
        } catch (RestClientException e) {
            logger.error("Failed to create order for user {}, type {}, on security {}: RestClientException error {}", userName, orderType,
                    securityName, e.getMessage());
            fail("RestClientException error during order creation: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error when creating order for user {}, type {}, on security {}: {}", userName,
                    orderType, securityName, e.getMessage());
            fail("Unexpected error during order creation: " + e.getMessage());
        }
    }

}
