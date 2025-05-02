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
            return http
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 서버는 세션 안 만들고 jwt로 인증하겠다.
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 🔥 모든 OPTIONS 요청 허용
                            .requestMatchers("/", "/index.html", "/stomptest.html", "/static/**").permitAll()
                            .requestMatchers("/ws/chat/**", "/sub/**", "/pub/**").permitAll()

                            // 회원가입, 로그인


                            .requestMatchers("/api/members/signup").permitAll()
                            .requestMatchers("/api/members/login").permitAll()

                            // 중복확인
                            .requestMatchers("/api/members/check-id").permitAll()
                            .requestMatchers("/api/members/check-nickname").permitAll()
                            .requestMatchers("/api/members/check-phone").permitAll()
                            .requestMatchers("/api/members/check-email").permitAll()

                            // 내 회원정보 조회
                            .requestMatchers("/api/members/me").authenticated()

                            // 회원 페이지 조회
                            .requestMatchers(HttpMethod.GET, "/api/memberpage/{memberId}").permitAll()

                            // 아이템
                            .requestMatchers(HttpMethod.GET, "/api/items/list").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/items/seller/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/items/search").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/items/**").permitAll()

                            .requestMatchers(HttpMethod.POST, "/api/items/new").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/items/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/items/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                            // 게시글
                            .requestMatchers(HttpMethod.GET, "/api/posts/list").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/posts/writer/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()

                            .requestMatchers(HttpMethod.POST, "/api/posts/new").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/posts/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/posts/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                            // 댓글
                            .requestMatchers(HttpMethod.POST, "/api/comments/{postNo}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/comments/{commentId}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/comments/{commentId}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/comments/{postNo}").permitAll()

                            // 주문
                            .requestMatchers(HttpMethod.POST, "/api/orders/new").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/orders/{orderId}/delivery").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/orders/{orderId}/cancel").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/orders/{orderId}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/orders/member/{memberId}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                            // 리뷰
                            .requestMatchers(HttpMethod.GET, "/api/store/reviews/**").permitAll()

                            // 신고하기
                            .requestMatchers(HttpMethod.POST, "/api/reports/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                            // 관리자
                            .requestMatchers(HttpMethod.POST, "/api/admin/new").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/admin/**").hasAuthority("ROLE_ADMIN")

                            // 채팅
                            .requestMatchers(HttpMethod.GET, "/api/chat/list/{memberId}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/chat/history/{sender}/{receiver}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .formLogin(formLogin -> formLogin.disable())
                    .build();
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
//            configuration.addAllowedOriginPattern("*"); // 🔥 모든 Origin 허용
            configuration.setAllowedOrigins(List.of("http://localhost:8010")); // allowCredntials(true) 쓰면 "*"와일드카드랑 같이 사용 불가.
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(List.of("*"));
            configuration.setAllowCredentials(true);
            configuration.setExposedHeaders(List.of("Authorization")); // 필요 시 추가 헤더 노출

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 CORS 설정 적용

            return source;
        }


    }
