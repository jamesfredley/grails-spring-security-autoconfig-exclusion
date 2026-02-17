package com.example

import grails.converters.JSON
import org.springframework.beans.factory.annotation.Autowired
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

        // Check which autoconfigurations are active by looking for their beans
        Map<String, Map> autoConfigsPresent = [:]
        [
            'SecurityAutoConfiguration': 'org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration',
            'SecurityFilterAutoConfiguration': 'org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration',
            'UserDetailsServiceAutoConfiguration': 'org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration',
        ].each { String name, String className ->
            try {
                Class clazz = Class.forName(className)
                autoConfigsPresent[name] = [
                    classOnClasspath: true,
                    beanExists: applicationContext.getBeanNamesForType(clazz).length > 0
                ]
            } catch (ClassNotFoundException ignored) {
                autoConfigsPresent[name] = [classOnClasspath: false, beanExists: false]
            }
        }

        boolean hasMultipleFilterChains = filterChainBeans.size() > 1
        boolean hasMultipleUserDetailsServices = udsBeans.size() > 1

        render([
            securityFilterChainBeans: filterChainBeans,
            userDetailsServiceBeans: udsBeans,
            autoConfigurationsStatus: autoConfigsPresent,
            multipleFilterChains: hasMultipleFilterChains,
            multipleUserDetailsServices: hasMultipleUserDetailsServices,
            note: 'The grails-spring-security plugin README requires 7 manual spring.autoconfigure.exclude entries that every user must add. The plugin should auto-exclude these via AutoConfigurationImportFilter SPI.'
        ] as JSON)
    }
}
