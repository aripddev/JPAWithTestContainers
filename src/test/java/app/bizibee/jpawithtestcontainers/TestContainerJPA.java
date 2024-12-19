package app.bizibee.jpawithtestcontainers;

import app.bizibee.jpawithtestcontainers.model.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestContainerJPA {
  
  private EntityManagerFactory emf;
  
  @Container
  private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>()
          .withDatabaseName("testdb")
            .withUsername("root")
            .withPassword("password");
            //.withInitScript("init.sql")
            //.withEnv("SOME_ENV_SETTING", "abc")
            //.withExposedPorts(5432, 1432)
            //.withCommand("--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci")
  
  @BeforeAll
  void setup() {
    System.setProperty("db.port", postgres.getFirstMappedPort().toString());
    emf = Persistence.createEntityManagerFactory("my-persistence");
  }
  
  @Test
  public void testCreateCustomer() {
    EntityManager entityManager = emf.createEntityManager();
    entityManager.getTransaction().begin();
    Customer c = new Customer();
    c.setFirstName("John");
    c.setLastName("Doe");
    c.setEmail("jdoe@gmail.com");
    entityManager.persist(c);
    entityManager.getTransaction().commit();
    entityManager.close();
    // Verify
    EntityManager em = emf.createEntityManager();
    List<Customer> customers = em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
    Assertions.assertEquals(1, customers.size());
    Assertions.assertEquals("John", customers.get(0).getFirstName());
    em.close();
  }
  
  @Test
  public void testDeleteCustomer() {
    EntityManager entityManager = emf.createEntityManager();
    entityManager.getTransaction().begin();
    Customer c = new Customer();
    c.setFirstName("Charlie");
    c.setLastName("Black");
    c.setEmail("charlie@gmail.com");
    entityManager.persist(c);
    entityManager.getTransaction().commit();
    // Delete
    entityManager.getTransaction().begin();
    entityManager.remove(entityManager.contains(c) ? c : entityManager.merge(c));
    entityManager.getTransaction().commit();
    entityManager.close();
    // Verify
    EntityManager em = emf.createEntityManager();
    Customer deletedCustomer = em.find(Customer.class, c.getId());
    Assertions.assertNull(deletedCustomer);
    em.close();
  }
}