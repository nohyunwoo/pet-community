package com.example.community.config;


import com.example.community.exception.CustomAccessDeniedHandler;
import com.example.community.exception.CustomAuthenticationEntryPoint;
import com.example.community.exception.GlobalExceptionHandler;
import com.example.community.security.MyUserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

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
            // 필터 단계에서 발생하는 진짜 에러를 여기서 로그로 남깁니다!
            log.error(">>> [LOGIN ERROR] 로그인 실패 원인: {}", exception.getMessage());

            // 로그를 남긴 후, 원래 설정했던 에러 페이지로 보냅니다.
            response.sendRedirect("/login?error=true");
        };
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
                                   "/banner.jpg","/favicon.ico").permitAll()
                                   
                    .anyRequest().authenticated()
        );

        // 2. [핵심] 예외 처리 핸들러를 시큐리티 필터 체인에 등록합니다.
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
