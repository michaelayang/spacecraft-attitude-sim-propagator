// Copyright (C) 2024, M. Yang 
// 
//     This program is free software: you can redistribute it and/or modify
//     it under the terms of the GNU General Public License as published by
//     the Free Software Foundation, either version 3 of the License, or
//     (at your option) any later version.
// 
//     This program is distributed in the hope that it will be useful, 
//     but WITHOUT ANY WARRANTY; without even the implied warranty of
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
//     GNU General Public License for more details.
// 
//     For further details, please see the README.md file included
//     with this software, and/or the GNU General Public License html
//     file which is also included with this software.

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
