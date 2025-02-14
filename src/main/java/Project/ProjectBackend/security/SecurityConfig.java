    package Project.ProjectBackend.security;

    import lombok.RequiredArgsConstructor;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.http.HttpMethod;
    import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
    import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
    import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
    import org.springframework.web.cors.CorsConfigurationSource;

    import java.util.List;

    @Configuration
    @EnableWebSecurity
    @EnableMethodSecurity(prePostEnabled = true) // 메소드 수준 보안 활성화
    @RequiredArgsConstructor
    public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        // 비밀번호 인코더 빈 정의
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        // AuthenticationManager 빈 정의
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
            return authenticationConfiguration.getAuthenticationManager();
        }

        // 보안 필터 체인 정의
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정
                    .csrf(csrf -> csrf.ignoringRequestMatchers("/ws/chat/**", "/pub/**", "/sub/**")) // ✅ WebSocket 관련 CSRF 제외
                    .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 사용으로 세션 비활성화
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/", "/index.html").permitAll()  // ✅ 루트 페이지 허용
                            .requestMatchers("/members/**").permitAll() // 인증 없이 접근 가능한 경로 설정
                            .requestMatchers("/ws/chat/**").permitAll() // WebSocket 엔드포인트 허용
                            .requestMatchers("/sub/**", "/pub/**").permitAll() // ✅ STOMP 메시지 허용

                            // 아이템
                            .requestMatchers(HttpMethod.GET, "/items/list").permitAll()
                            .requestMatchers(HttpMethod.GET,"/items/seller/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/items/search").permitAll()
                            .requestMatchers(HttpMethod.GET, "/items/**").permitAll()

                            .requestMatchers(HttpMethod.POST, "/items/new").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/items/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/items/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                            // 게시글
                            .requestMatchers(HttpMethod.GET, "/posts/list").permitAll()
                            .requestMatchers(HttpMethod.GET, "/posts/writer/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()

                            .requestMatchers(HttpMethod.POST, "/posts/new").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/posts/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/posts/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")


                            // 댓글
                            .requestMatchers(HttpMethod.POST, "/comments/{postNo}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/comments/{commentId}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/comments/{commentId}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.GET, "/comments/{postNo}").permitAll()



                            // 주문
                            .requestMatchers(HttpMethod.POST, "/orders/new").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/orders/{orderId}/delivery").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.POST, "/orders/{orderId}/cancel").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.GET, "/orders/{orderId}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.GET, "/orders/member/{memberId}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                            // 리뷰
                            .requestMatchers(HttpMethod.GET, "/store/reviews/**").permitAll()

                            // 신고하기
                            .requestMatchers(HttpMethod.POST, "/reports/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")


                            // 관리자
                            .requestMatchers(HttpMethod.POST, "/admin/new").permitAll()
                            .requestMatchers(HttpMethod.GET, "/admin/**").hasAuthority("ROLE_ADMIN")

                            // 채팅
                            .requestMatchers(HttpMethod.GET, "/chat/list/{memberId}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.GET, "/chat/history/{sender}/{receiver}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")


                            .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                    )
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // JWT 필터 추가
                    .formLogin(formLogin -> formLogin.disable()); // 기본 로그인 폼 비활성화

            return http.build();
        }

        // 역할 계층 설정
        @Bean
        public RoleHierarchy roleHierarchy() {
            RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
            roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
            return roleHierarchy;
        }

        // CORS 설정을 위한 CorsConfigurationSource 빈 정의
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(List.of("http://localhost:8010", "http://localhost:63342"));
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(List.of("*"));
            configuration.setAllowCredentials(true);
            configuration.setExposedHeaders(List.of("Authorization")); // 필요 시 추가 헤더 노출

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 CORS 설정 적용

            return source;
        }


    }
