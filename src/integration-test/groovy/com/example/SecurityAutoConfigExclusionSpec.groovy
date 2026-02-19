package com.example

import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport
import org.springframework.context.ApplicationContext
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.SecurityFilterChain
import spock.lang.Specification

@Integration
class SecurityAutoConfigExclusionSpec extends Specification {

    @Autowired
    ApplicationContext applicationContext

    void "SecurityAutoConfigurationExcluder is on the classpath"() {
        expect:
        Class.forName('grails.plugin.springsecurity.SecurityAutoConfigurationExcluder')
    }

    void "security auto-configurations are excluded by the filter"() {
        given:
        ConditionEvaluationReport report = ConditionEvaluationReport.get(applicationContext.autowireCapableBeanFactory)
        List<String> exclusions = report.exclusions?.collect { it } ?: []

        expect: "none of the 7 conflicting auto-configs are loaded"
        !exclusions.contains('org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration') ||
            exclusions.contains('org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration')
    }

    void "no duplicate SecurityFilterChain beans exist"() {
        given:
        String[] filterChainBeans = applicationContext.getBeanNamesForType(SecurityFilterChain)

        expect: "only the Grails plugin's filter chain, not Spring Boot's default"
        filterChainBeans.length <= 1
    }

    void "no duplicate UserDetailsService beans from auto-configuration"() {
        given:
        String[] udsBeans = applicationContext.getBeanNamesForType(UserDetailsService)

        expect: "only the Grails plugin's GormUserDetailsService"
        udsBeans.length >= 1
        udsBeans.any { it.toLowerCase().contains('userdetails') || it.toLowerCase().contains('gorm') }
    }

    void "SecurityAutoConfiguration bean is not registered"() {
        given:
        Class secAutoConfig = Class.forName('org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration')

        expect:
        applicationContext.getBeanNamesForType(secAutoConfig).length == 0
    }

    void "SecurityFilterAutoConfiguration bean is not registered"() {
        given:
        Class secFilterAutoConfig = Class.forName('org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration')

        expect:
        applicationContext.getBeanNamesForType(secFilterAutoConfig).length == 0
    }
}
