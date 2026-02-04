package com.example.community.config;


import com.example.community.exception.CustomAccessDeniedHandler;
import com.example.community.exception.CustomAuthenticationEntryPoint;
import com.example.community.security.MyUserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final MyUserDetailService myUserDetailService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            log.error(">>> [LOGIN ERROR] 로그인 실패 원인: {}", exception.getMessage());

            response.sendRedirect("/login?error=true");
        };
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .requestMatchers("/favicon.ico", "/images/**", "/css/**", "/js/**", "/banner.jpg", "/upload/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        http.csrf(csrf -> csrf
                        .ignoringRequestMatchers(PathRequest.toStaticResources().atCommonLocations())
                        .ignoringRequestMatchers("/register", "/login", "/post/like")
                );

        http.authorizeHttpRequests(authorize ->
                authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                        .requestMatchers("/", "/home", "/board/**", "/login", "/register").permitAll()
                        .anyRequest().authenticated()
        );

        http.exceptionHandling(handler -> handler
                .authenticationEntryPoint(customAuthenticationEntryPoint) // 401: 인증 실패 시 실행
                .accessDeniedHandler(customAccessDeniedHandler)           // 403: 권한 부족 시 실행
        );


        http.formLogin((formLogin) ->
                formLogin
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .failureHandler(failureHandler())
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
