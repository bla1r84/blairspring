package com.blair.blairspring;

import com.blair.blairspring.model.ibatisschema.Job;
import com.blair.blairspring.model.ibatisschema.Nationality;
import com.blair.blairspring.model.ibatisschema.Player;
import com.blair.blairspring.repositories.ibatisschema.jpa.PlayerRepository;
import com.blair.blairspring.util.TestComponent;
import com.blair.blairspring.util.lookup.SingletonBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.Persistence;
import javax.persistence.PersistenceUtil;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles({"jpa", "ibatis", "dev"})
@TestPropertySource(properties = {"name=Kostas", "age=50"}, locations = "classpath:actuator.properties")
@Slf4j
class BlairspringApplicationTests {
    
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${local.management.port}")
    private int serverPort;

    @Value("${name}")
    private String name;

    @Value("${age}")
    private int age;

    @LocalServerPort
    int randomServerPort;

    private String managementUrl = "http://localhost";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestComponent testComponent;

    @Autowired
    private PlayerRepository playerRepository;

    @BeforeAll
    static void init() {

    }

    @BeforeEach
    void setUp() {
        managementUrl = managementUrl + ":" + serverPort + "/blairactuator";

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(HAL_JSON));
        converter.setObjectMapper(objectMapper);
        restTemplate = restTemplate.withBasicAuth("john", "doe");
    }

    @Test
    void testGetJob() {
        String url = "http://localhost:" + randomServerPort + "/jobs/1";
        ResponseEntity<Job> responseEntity = restTemplate.getForEntity(url, Job.class);
        log.info("Retrieved Job: " + responseEntity.getBody().getDescription());
        //ResponseEntity<Job> jobResponseEntity = restTemplate.getForObject(url, Job.class);
    }

    @Test
    void testPostJob() {
        HttpEntity<Job> request = new HttpEntity<>(new Job("Policeman"));

        assertThrows(ConstraintViolationException.class,
                () -> restTemplate.postForLocation("http://localhost:" + randomServerPort + "/jobs", request));
    }

    @Test
    void testCreateReadUpdateDelete() {
        String url = "http://localhost:" + randomServerPort + "/jobs";

        HttpEntity<Job> request = new HttpEntity<>(new Job("Policeman2"));
        ResponseEntity<Job> response = restTemplate.exchange(url, HttpMethod.POST, request, Job.class);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

        HttpHeaders httpHeaders = restTemplate.headForHeaders(url);
        assertTrue(httpHeaders.getContentType().includes(HAL_JSON));
        assertNotNull(restTemplate.getForObject(url + "/{id}", Job.class, response.getBody().getId()));
    }

	@Test
    void shouldReturn200WhenSendingRequestToHealthEndpoint() {
        ResponseEntity<Map> entity = restTemplate
                .withBasicAuth("admin", "admin")
                .getForEntity(managementUrl + "/health", Map.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK, entity.getStatusCode())
        );
    }

    @Test
    void testUserController() {
        String uriTemplate = "http://localhost:" + randomServerPort + "/users";
        URI uri = UriComponentsBuilder.fromUriString(uriTemplate).build().toUri();

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, null, String.class);
        log.info("body: {}", response.getBody());
    }

    @Test
    void testTestComponent() {
        testComponent.getTestBean().testMethod();
        testComponent.getTestBean().testMethod();
    }

    @Test
    void testLookup() {
        SingletonBean singletonBean = applicationContext.getBean(SingletonBean.class);
        assertNotEquals(singletonBean.usePrototype(), singletonBean.usePrototype());
    }

    @Test
    void testProperties() {
        assertAll(
                () -> assertNotNull(name),
                () -> assertNotNull(age)
        );
        log.info("Name: {}, Age: {}", name, age);
    }

    @Test
    void testLazyFetch() {
        Player player = playerRepository.findById(10L).get();
        PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
        log.info("nationalities loaded: {}", persistenceUtil.isLoaded(player.getNationalities()));
        Nationality nationality = player.getNationalities().stream().findFirst().get();
        log.info("nationalities loaded: {}", persistenceUtil.isLoaded(player.getNationalities()));
        log.info("nationality: {}", nationality.getName());
        log.info("nationalities loaded: {}", persistenceUtil.isLoaded(player.getNationalities()));
    }
}
