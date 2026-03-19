package com.connectfood.order.infrastructure.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.connectfood.order.application.dto.OrderOutput;
import com.connectfood.order.application.usecase.CreateOrderUseCase;
import com.connectfood.order.application.usecase.GetOrderByUuidUseCase;
import com.connectfood.order.application.usecase.ListOrdersByCustomerUseCase;
import com.connectfood.order.application.usecase.UpdateOrderStatusUseCase;
import com.connectfood.order.domain.model.OrderStatus;
import com.connectfood.order.entrypoint.rest.controller.GlobalExceptionHandler;
import com.connectfood.order.entrypoint.rest.controller.OrderController;
import com.connectfood.order.infrastructure.security.JwtValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = {OrderSecurityConfigTest.TestApplication.class, OrderSecurityConfigTest.TestConfig.class},
    properties = {
        "security.jwt.issuer=test-issuer",
        "security.jwt.secret=CHANGE_ME_TO_A_LONG_RANDOM_SECRET_32+_CHARS",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration,org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"
    }
)
class OrderSecurityConfigTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private JwtValidator jwtValidator;

  @Autowired
  private CreateOrderUseCase createOrderUseCase;

  @Autowired
  private GetOrderByUuidUseCase getOrderByUuidUseCase;

  @Autowired
  private ListOrdersByCustomerUseCase listOrdersByCustomerUseCase;

  @Autowired
  private UpdateOrderStatusUseCase updateOrderStatusUseCase;

  private MockMvc mockMvc;

  private final UUID customerUuid = UUID.randomUUID();
  private final UUID adminUuid = UUID.randomUUID();
  private final UUID ownerUuid = UUID.randomUUID();
  private final UUID orderUuid = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();

    Mockito.reset(jwtValidator, createOrderUseCase, getOrderByUuidUseCase, listOrdersByCustomerUseCase, updateOrderStatusUseCase);

    Mockito.when(jwtValidator.validateAndExtract("customer-token"))
        .thenReturn(new JwtValidator.JwtPrincipal(customerUuid, Set.of("CUSTOMER")));
    Mockito.when(jwtValidator.validateAndExtract("admin-token"))
        .thenReturn(new JwtValidator.JwtPrincipal(adminUuid, Set.of("ADMIN")));
    Mockito.when(jwtValidator.validateAndExtract("owner-token"))
        .thenReturn(new JwtValidator.JwtPrincipal(ownerUuid, Set.of("RESTAURANT_OWNER")));

    var output = new OrderOutput(
        orderUuid,
        customerUuid,
        "rest-1",
        OrderStatus.CREATED,
        java.math.BigDecimal.TEN,
        List.of()
    );

    Mockito.when(getOrderByUuidUseCase.execute(orderUuid))
        .thenReturn(output);
    Mockito.when(listOrdersByCustomerUseCase.execute(ArgumentMatchers.any()))
        .thenReturn(List.of(output));
    Mockito.when(createOrderUseCase.execute(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(output);
  }

  @Test
  void shouldForbidCustomerFromListingOrders() throws Exception {
    mockMvc.perform(get("/orders")
            .header(HttpHeaders.AUTHORIZATION, "Bearer customer-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowAdminToListOrders() throws Exception {
    mockMvc.perform(get("/orders")
            .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
        .andExpect(status().isOk());
  }

  @Test
  void shouldAllowRestaurantOwnerToListOrders() throws Exception {
    mockMvc.perform(get("/orders")
            .header(HttpHeaders.AUTHORIZATION, "Bearer owner-token"))
        .andExpect(status().isOk());
  }

  @Test
  void shouldAllowCustomerToGetOrderByUuid() throws Exception {
    mockMvc.perform(get("/orders/{uuid}", orderUuid)
            .header(HttpHeaders.AUTHORIZATION, "Bearer customer-token"))
        .andExpect(status().isOk());
  }

  @Test
  void shouldAllowCustomerToCreateOrder() throws Exception {
    mockMvc.perform(post("/orders")
            .with(csrf().asHeader())
            .header(HttpHeaders.AUTHORIZATION, "Bearer customer-token")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .content("""
                {
                  "restaurantId": "rest-1",
                  "items": [
                    {
                      "itemId": "item-1",
                      "itemName": "Item 1",
                      "quantity": 1,
                      "unitPrice": 10.00
                    }
                  ]
                }
                """))
        .andExpect(status().isCreated());
  }

  @TestConfiguration
  static class TestConfig {

    @Bean
    @Primary
    JwtValidator jwtValidator() {
      return Mockito.mock(JwtValidator.class);
    }

    @Bean
    @Primary
    CreateOrderUseCase createOrderUseCase() {
      return Mockito.mock(CreateOrderUseCase.class);
    }

    @Bean
    @Primary
    GetOrderByUuidUseCase getOrderByUuidUseCase() {
      return Mockito.mock(GetOrderByUuidUseCase.class);
    }

    @Bean
    @Primary
    ListOrdersByCustomerUseCase listOrdersByCustomerUseCase() {
      return Mockito.mock(ListOrdersByCustomerUseCase.class);
    }

    @Bean
    @Primary
    UpdateOrderStatusUseCase updateOrderStatusUseCase() {
      return Mockito.mock(UpdateOrderStatusUseCase.class);
    }
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import({SecurityConfig.class, OrderController.class, GlobalExceptionHandler.class})
  static class TestApplication {
  }
}
