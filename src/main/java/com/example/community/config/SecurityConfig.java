package com.example.community.config;


import com.example.community.security.MyUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
// 설정을 활성화
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MyUserDetailService myUserDetailService;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(authorize ->
                authorize
                    .requestMatchers("/", "/home", "/board/**", "/login", "/register", 
                                   "/css/**", "/js/**", "/images/**",
                                   "/*.css", "/*.js", "/*.jpg", "/*.png", "/*.jpeg", "/*.gif",
                                   "/nav.css", "/home.css", "/board.css", "/post.css", "/postview.css", "/profile.css",
                                   "/banner.jpg").permitAll()
                                   
                    .anyRequest().authenticated()
        );


        http.formLogin((formLogin) ->
                formLogin
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
        );

        http.logout(logout ->
            logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/home")
                    .permitAll()
    );
        return http.build();
    }
}
