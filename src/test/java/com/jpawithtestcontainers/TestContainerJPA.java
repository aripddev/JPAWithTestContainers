package com.jpawithtestcontainers;

import com.jpawithtestcontainers.model.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestContainerJPA {

  private EntityManagerFactory emf;

  @Container
  private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3")
          .withDatabaseName("testdb")
          .withUsername("root")
          .withPassword("password");

  @BeforeAll
  void setup() {
    // Start the container
    postgres.start();

    // Create properties for EntityManagerFactory
    java.util.Map<String, String> properties = new java.util.HashMap<>();
    properties.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
    properties.put("jakarta.persistence.jdbc.url", postgres.getJdbcUrl());
    properties.put("jakarta.persistence.jdbc.user", postgres.getUsername());
    properties.put("jakarta.persistence.jdbc.password", postgres.getPassword());

    // Initialize the EntityManagerFactory with properties
    emf = Persistence.createEntityManagerFactory("my-persistence", properties);
  }

  @AfterAll
  void cleanup() {
    // Close the EntityManagerFactory and stop the container
    if (emf != null && emf.isOpen()) {
      emf.close();
    }
    postgres.stop();
  }

  @Test
  public void testCreateCustomer() {
    EntityManager entityManager = emf.createEntityManager();
    try {
      entityManager.getTransaction().begin();
      Customer c = new Customer();
      c.setFirstName("John");
      c.setLastName("Doe");
      c.setEmail("jdoe@gmail.com");
      entityManager.persist(c);
      entityManager.getTransaction().commit();
    } finally {
      entityManager.close();
    }

    // Verify
    EntityManager verifyManager = emf.createEntityManager();
    try {
      List<Customer> customers = verifyManager.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
      Assertions.assertEquals(1, customers.size());
      Assertions.assertEquals("John", customers.get(0).getFirstName());
    } finally {
      verifyManager.close();
    }
  }

  @Test
  public void testDeleteCustomer() {
    EntityManager entityManager = emf.createEntityManager();
    Customer c = new Customer();
    try {
      // Create a new customer
      entityManager.getTransaction().begin();
      c.setFirstName("Charlie");
      c.setLastName("Black");
      c.setEmail("charlie@gmail.com");
      entityManager.persist(c);
      entityManager.getTransaction().commit();

      // Delete the customer
      entityManager.getTransaction().begin();
      entityManager.remove(entityManager.contains(c) ? c : entityManager.merge(c));
      entityManager.getTransaction().commit();
    } finally {
      entityManager.close();
    }

    // Verify
    EntityManager verifyManager = emf.createEntityManager();
    try {
      Customer deletedCustomer = verifyManager.find(Customer.class, c.getId());
      Assertions.assertNull(deletedCustomer);
    } finally {
      verifyManager.close();
    }
  }
}
