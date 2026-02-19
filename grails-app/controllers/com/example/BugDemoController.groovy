package com.example

import grails.converters.JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport
import org.springframework.context.ApplicationContext

class BugDemoController {

    @Autowired
    ApplicationContext applicationContext

    def index() {
        // Check which SecurityFilterChain beans exist
        def filterChainBeans = applicationContext.getBeanNamesForType(
            Class.forName('org.springframework.security.web.SecurityFilterChain')
        ) as List<String>

        // Check if UserDetailsService has duplicate beans
        def udsBeans = applicationContext.getBeanNamesForType(
            Class.forName('org.springframework.security.core.userdetails.UserDetailsService')
        ) as List<String>

        boolean excluderOnClasspath = false
        try {
            Class.forName('grails.plugin.springsecurity.SecurityAutoConfigurationExcluder')
            excluderOnClasspath = true
        } catch (ClassNotFoundException ignored) {}

        List<String> excludedAutoConfigs = []
        try {
            ConditionEvaluationReport report = ConditionEvaluationReport.get(applicationContext.autowireCapableBeanFactory)
            excludedAutoConfigs = report.exclusions?.collect { it } ?: []
        } catch (Exception ignored) {}

        // Check which autoconfigurations are active by looking for their beans
        Map<String, Map> autoConfigStatus = [:]
        [
            'SecurityAutoConfiguration': 'org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration',
            'SecurityFilterAutoConfiguration': 'org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration',
            'UserDetailsServiceAutoConfiguration': 'org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration',
        ].each { String name, String className ->
            try {
                Class clazz = Class.forName(className)
                autoConfigStatus[name] = [
                    classOnClasspath: true,
                    beanExists: applicationContext.getBeanNamesForType(clazz).length > 0,
                    filteredOut: excludedAutoConfigs.contains(className)
                ]
            } catch (ClassNotFoundException ignored) {
                autoConfigStatus[name] = [classOnClasspath: false, beanExists: false, filteredOut: false]
            }
        }

        boolean hasMultipleFilterChains = filterChainBeans.size() > 1
        boolean hasMultipleUserDetailsServices = udsBeans.size() > 1

        render([
            excluderOnClasspath: excluderOnClasspath,
            securityFilterChainBeans: filterChainBeans,
            securityFilterChainCount: filterChainBeans.size(),
            userDetailsServiceBeans: udsBeans,
            userDetailsServiceCount: udsBeans.size(),
            autoConfigurationsStatus: autoConfigStatus,
            excludedAutoConfigurations: excludedAutoConfigs,
            multipleFilterChains: hasMultipleFilterChains,
            multipleUserDetailsServices: hasMultipleUserDetailsServices,
        ] as JSON)
    }
}
