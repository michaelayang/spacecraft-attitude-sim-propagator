package com.spacecraftpropagator.services;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.inMemoryAuthentication()
                .withUser("user").password("{sha256}easy_password").roles("USER")
                .and()
                .withUser("admin").password("{noop}tough_password").roles("USER", "ADMIN");

    }

    // Secure PUT endpoints with HTTP Basic authentication (allow GET endpoints to function without authentication)
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                //HTTP Basic authentication
                .httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.PUT, "/loadKeplerianRecords/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/loadPtolemaicRecords/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/loadTruthDataRecords/**").hasRole("ADMIN")
                .and()
                .csrf().disable()
                .formLogin().disable();
    }

}
