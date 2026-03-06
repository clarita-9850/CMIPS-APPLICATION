package com.clarita.governance.spring;

import com.clarita.governance.core.GovernanceEngine;
import com.clarita.governance.core.config.GovernanceConfig;
import com.clarita.governance.core.scanner.AnnotationScanner;
import com.clarita.governance.core.validator.GovernanceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the Test Governance Framework.
 *
 * <p>This configuration is automatically applied when the starter is on the classpath
 * and governance is enabled (which is the default).</p>
 *
 * <h2>Enabling/Disabling</h2>
 * <pre>{@code
 * # Enable (default)
 * governance.enabled=true
 *
 * # Disable
 * governance.enabled=false
 * }</pre>
 *
 * <h2>Provided Beans</h2>
 * <ul>
 *   <li>{@link GovernanceConfig} - Configuration object</li>
 *   <li>{@link AnnotationScanner} - Classpath scanner</li>
 *   <li>{@link GovernanceValidator} - Validation engine</li>
 *   <li>{@link GovernanceEngine} - Main entry point</li>
 * </ul>
 *
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(GovernanceProperties.class)
@ConditionalOnProperty(name = "governance.enabled", havingValue = "true", matchIfMissing = true)
public class GovernanceAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GovernanceAutoConfiguration.class);

    private final GovernanceProperties properties;

    public GovernanceAutoConfiguration(GovernanceProperties properties) {
        this.properties = properties;
        log.info("Test Governance Framework auto-configuration initialized");
    }

    /**
     * Creates the governance configuration from Spring properties.
     *
     * @return governance config
     */
    @Bean
    @ConditionalOnMissingBean
    public GovernanceConfig governanceConfig() {
        log.debug("Creating GovernanceConfig from properties");
        return properties.toConfig();
    }

    /**
     * Creates the annotation scanner.
     *
     * @param config the governance config
     * @return scanner
     */
    @Bean
    @ConditionalOnMissingBean
    public AnnotationScanner annotationScanner(GovernanceConfig config) {
        log.debug("Creating AnnotationScanner");
        return new AnnotationScanner(config);
    }

    /**
     * Creates the governance validator.
     *
     * @param config the governance config
     * @param scanner the annotation scanner
     * @return validator
     */
    @Bean
    @ConditionalOnMissingBean
    public GovernanceValidator governanceValidator(GovernanceConfig config, AnnotationScanner scanner) {
        log.debug("Creating GovernanceValidator");
        return new GovernanceValidator(config, scanner);
    }

    /**
     * Creates the main governance engine.
     *
     * @param config the governance config
     * @return engine
     */
    @Bean
    @ConditionalOnMissingBean
    public GovernanceEngine governanceEngine(GovernanceConfig config) {
        log.debug("Creating GovernanceEngine");
        return new GovernanceEngine(config);
    }
}
