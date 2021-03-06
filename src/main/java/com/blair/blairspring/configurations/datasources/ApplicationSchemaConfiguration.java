package com.blair.blairspring.configurations.datasources;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.blair.blairspring.repositories.ibatisschema",
        entityManagerFactoryRef = "applicationEntityManagerFactoryBean")
public class ApplicationSchemaConfiguration {

    private final Environment env;

    private final AtomikosDataSourceBean atomikosDataSourceBean;

    public ApplicationSchemaConfiguration(
            Environment env,
            @Qualifier("applicationDataSource") AtomikosDataSourceBean atomikosDataSourceBean) {
        this.env = env;
        this.atomikosDataSourceBean = atomikosDataSourceBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean applicationEntityManagerFactoryBean() throws SQLException {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setPackagesToScan("com.blair.blairspring.model.ibatisschema");
        factoryBean.setJpaDialect(new HibernateJpaDialect());
        factoryBean.setDataSource(atomikosDataSourceBean);

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", env.getProperty("spring.jpa.database-platform"));
        jpaProperties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        jpaProperties.put("hibernate.format_sql", "true");

        factoryBean.setJpaProperties(jpaProperties);

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(true);
        factoryBean.setJpaVendorAdapter(vendorAdapter);

        Map<String, String> jpaPropertiesMap = new HashMap<>();
        jpaPropertiesMap.put("javax.persistence.transactionType", "JTA");
        jpaPropertiesMap.put("hibernate.current_session_context_class", "jta");
        jpaPropertiesMap.put("hibernate.transaction.manager_lookup_class", "com.atomikos.icatch.jta.hibernate3.TransactionManagerLookup");
        jpaPropertiesMap.put("hibernate.connection.autocommit", "false");

        factoryBean.setJpaPropertyMap(jpaPropertiesMap);

        factoryBean.afterPropertiesSet();
        return factoryBean;
    }

    @Profile("jdbc")
    @Bean
    public JdbcTemplate jdbcTemplate() throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(atomikosDataSourceBean);
        return jdbcTemplate;
    }

    @Profile("jdbc")
    @Bean
    public SimpleJdbcInsert simpleJdbcInsert() throws SQLException {
        return new SimpleJdbcInsert(atomikosDataSourceBean);
    }

}
