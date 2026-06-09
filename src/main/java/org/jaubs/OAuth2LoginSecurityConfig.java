package org.jaubs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class OAuth2LoginSecurityConfig {

    private static final List<String> SOCIAL_PROVIDERS = List.of("Google", "Facebook", "GitLab", "GitHub");

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

            http
                .authorizeHttpRequests(authorize ->
                        authorize
                            .requestMatchers("/main.html", "/css/**", "/js/**", "/images/**").permitAll()
                            .requestMatchers("/jaubs/ui").authenticated()
                            .requestMatchers("/jaubs/ui/admin/**").hasAnyRole("jaubs-admin")
                            .anyRequest().hasAnyRole("jaubs-admin", "jaubs-user"))
                .oauth2Login(oauth2 ->
                        oauth2
                            .userInfoEndpoint(epConfig -> epConfig.oidcUserService(this.userService()))
                            .authorizationEndpoint(cfg -> cfg.authorizationRequestResolver(pkceResolver(clientRegistrationRepository))))
                .logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler()));

            return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);

        // Sets the location that the End-User's User Agent will be redirected to
        // after the logout has been performed at the Provider
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/main.html");
        return oidcLogoutSuccessHandler;
    }

    public OAuth2AuthorizationRequestResolver pkceResolver(ClientRegistrationRepository repo) {
        var resolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
        return resolver;
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> userService() {

        final OidcUserService delegate = new OidcUserService();
        return (userRequest) -> {
            // Delegate to the default implementation for loading a user
            OidcUser oidcUser = delegate.loadUser(userRequest);

            OAuth2AccessToken accessToken = userRequest.getAccessToken();
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            // Handle different clients
            ClientRegistration cliReg = userRequest.getClientRegistration();
            if (isSocial(cliReg.getClientName())) {
                mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_jaubs-user"));
            }
            else {
                List<String> roles = (List<String>) oidcUser.getIdToken().getClaim("roles");
                if (roles == null) {
                    roles = List.of();
                }

                List<SimpleGrantedAuthority> listAuthorities
                        = roles.stream().map(SimpleGrantedAuthority::new).toList();
                mappedAuthorities.addAll(listAuthorities);
            }

            // 1) Fetch the authority information from the protected resource using accessToken
            // 2) Map the authority information to one or more GrantedAuthority's and add it to mappedAuthorities
            // 3) Create a copy of oidcUser but use the mappedAuthorities instead
            return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        };

    }

    private boolean isSocial(String clientName) {
        return (SOCIAL_PROVIDERS.contains(clientName));
    }
}
