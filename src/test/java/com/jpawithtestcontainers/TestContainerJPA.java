package com.jpawithtestcontainers;

import com.jpawithtestcontainers.model.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestContainerJPA {

  private EntityManagerFactory emf;
  private Customer initialUser;

  @Container
  private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3")
          .withDatabaseName("testdb")
          .withUsername("root")
          .withPassword("password");

  @BeforeAll
  void setup() {
    postgres.start();

    var properties = new java.util.HashMap<String, String>();
    properties.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
    properties.put("jakarta.persistence.jdbc.url", postgres.getJdbcUrl());
    properties.put("jakarta.persistence.jdbc.user", postgres.getUsername());
    properties.put("jakarta.persistence.jdbc.password", postgres.getPassword());

    emf = Persistence.createEntityManagerFactory("my-persistence", properties);
  }

  @BeforeEach
  void setupTest() {
    // Initialize database with initial user
    EntityManager em = emf.createEntityManager();
    try {
      em.getTransaction().begin();
      
      // Clear any existing data
      em.createQuery("DELETE FROM Customer").executeUpdate();
      
      // Create initial user
      initialUser = new Customer();
      initialUser.setFirstName("Initial");
      initialUser.setLastName("User");
      initialUser.setEmail("initial.user@example.com");
      em.persist(initialUser);
      em.getTransaction().commit();
    } finally {
      em.close();
    }
  }

  @AfterEach
  void cleanupTest() {
    // Clean up test data
    EntityManager em = emf.createEntityManager();
    try {
      em.getTransaction().begin();
      em.createQuery("DELETE FROM Customer").executeUpdate();
      em.getTransaction().commit();
    } finally {
      em.close();
    }
  }

  @AfterAll
  void cleanup() {
    if (emf != null && emf.isOpen()) {
      emf.close();
    }
    postgres.stop();
  }

  @Test
  public void testCreateCustomer() {
    EntityManager entityManager = emf.createEntityManager();
    Customer customer = new Customer();
    customer.setFirstName("John");
    customer.setLastName("Doe");
    customer.setEmail("jdoe@gmail.com");

    try {
      entityManager.getTransaction().begin();
      entityManager.persist(customer);
      entityManager.getTransaction().commit();
    } finally {
      entityManager.close();
    }

    // Verify
    EntityManager verifyManager = emf.createEntityManager();
    try {
      List<Customer> customers = verifyManager.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
      Assertions.assertEquals(2, customers.size(), "Should have initial user and new customer");
      
      Customer savedCustomer = customers.stream()
          .filter(c -> c.getEmail().equals("jdoe@gmail.com"))
          .findFirst()
          .orElseThrow();
          
      Assertions.assertAll("Customer properties",
          () -> Assertions.assertEquals("John", savedCustomer.getFirstName()),
          () -> Assertions.assertEquals("Doe", savedCustomer.getLastName()),
          () -> Assertions.assertEquals("jdoe@gmail.com", savedCustomer.getEmail())
      );
    } finally {
      verifyManager.close();
    }
  }

  @Test
  public void testDeleteCustomer() {
    EntityManager entityManager = emf.createEntityManager();
    Customer customer = new Customer();
    customer.setFirstName("Charlie");
    customer.setLastName("Black");
    customer.setEmail("charlie@gmail.com");

    try {
      // Create a new customer
      entityManager.getTransaction().begin();
      entityManager.persist(customer);
      entityManager.getTransaction().commit();

      // Delete the customer
      entityManager.getTransaction().begin();
      entityManager.remove(entityManager.contains(customer) ? customer : entityManager.merge(customer));
      entityManager.getTransaction().commit();
    } finally {
      entityManager.close();
    }

    // Verify
    EntityManager verifyManager = emf.createEntityManager();
    try {
      Customer deletedCustomer = verifyManager.find(Customer.class, customer.getId());
      Assertions.assertNull(deletedCustomer, "Customer should be deleted");
      
      // Verify initial user still exists
      List<Customer> remainingCustomers = verifyManager.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
      Assertions.assertEquals(1, remainingCustomers.size(), "Only initial user should remain");
      Assertions.assertEquals("initial.user@example.com", remainingCustomers.get(0).getEmail());
    } finally {
      verifyManager.close();
    }
  }
}
