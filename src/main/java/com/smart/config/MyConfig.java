package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class MyConfig{
	
	@Bean
	public UserDetailsService getUserDetailService() {
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.getUserDetailService());
		daoAuthenticationProvider.setPasswordEncoder(this.passwordEncoder());
		return daoAuthenticationProvider;
	}
	
	//configure method
	protected void configure(AuthenticationManagerBuilder auth) throws Exception{
		//since we are using database to fetch value therefore we use this
		auth.authenticationProvider(authenticationProvider());
		
	}
	// In your Spring Security configuration class
	@Bean
	public SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> securityConfigurerAdapter() {
	    return new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
	        @Override
	        public void configure(HttpSecurity http) throws Exception {
	            http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
	        }
	    };
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		.authorizeHttpRequests(authorize->authorize
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/user/**").hasRole("USER")
				.requestMatchers("/**").permitAll()
			
		)
		.formLogin(formLogin -> formLogin
			.loginPage("/signin")
			.loginProcessingUrl("/doLogin")
			.defaultSuccessUrl("/user/dashboard")
			
			.permitAll()
		);
		
		
		
		return http.build();
	}
		
}



/*
@Configuration
@EnableWebSecurity
public class MyConfig extends WebSecurityConfigurerAdapter{
	
	@Bean
	public UserDetailsService getUserDetailService() {
		return new UserDetailsServiceImpl();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.getUserDetailService());
		daoAuthenticationProvider.setPasswordEncoder(this.passwordEncoder());
		return daoAuthenticationProvider;
	}
	
	//configure method
	protected void configure(AuthenticationManagerBuilder auth) throws Exception{
		//since we are using database to fetch value therefore we use this
		auth.authenticationProvider(authenticationProvider());
		
	}
	//this is to tell which one to protect
	protected void configure(HttpSecurity http) throws Exception{
		http.authorizeRequests().antMatchers("/admin/**").hasRole("ADMIN")			//authorize only those users to access any website like admin/  who has their role set as admin
		.antMatchers("/user/**").hasRole("USER")									//authorize only those users to access any website like user/  who has their role set as user
		.antMatchers("/**").permitAll()												//auhtorize everyone
		.and().formLogin()															//form based login will be done
		.and().csrf().disable();
		
	}
	
	
	
	
	
}
*/