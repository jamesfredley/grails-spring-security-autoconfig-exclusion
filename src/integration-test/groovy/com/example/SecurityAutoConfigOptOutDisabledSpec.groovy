package com.example

import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

/**
 * Proves that setting excludeSpringSecurityAutoConfiguration=false disables the filter,
 * allowing Spring Boot's SecurityAutoConfiguration to load. Contrast with
 * SecurityAutoConfigExclusionSpec where the default (true) keeps it excluded.
 */
@Integration
@TestPropertySource(properties = 'grails.plugin.springsecurity.excludeSpringSecurityAutoConfiguration=false')
class SecurityAutoConfigOptOutDisabledSpec extends Specification {

    @Autowired
    ApplicationContext applicationContext

    void "SecurityAutoConfiguration IS loaded when excludeSpringSecurityAutoConfiguration is false"() {
        given: "the filter is disabled via the property"
        Class secAutoConfig = Class.forName('org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration')

        expect: "Spring Boot's auto-config is NOT filtered out and registers as a bean"
        applicationContext.getBeanNamesForType(secAutoConfig).length > 0
    }
}
