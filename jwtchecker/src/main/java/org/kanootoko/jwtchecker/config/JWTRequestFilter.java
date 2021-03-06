package org.kanootoko.jwtchecker.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kanootoko.jwtchecker.config.implementations.GrantedAuthorityImpl;
import org.kanootoko.jwtchecker.utils.JWTUtilSimplified;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JWTRequestFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		final String requestTokenHeader = request.getHeader("Authorization");

		if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
			String jwt = requestTokenHeader.substring(7);
			try {
                if (JWTUtilSimplified.validateToken(jwt)) {
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new GrantedAuthorityImpl(JWTUtilSimplified.getAuthorityFromToken(jwt)));
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            JWTUtilSimplified.getLoginFromToken(jwt), null, authorities);
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                } else {
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token has expired");
				}
			} catch (Exception ex) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access token is not valid");
			}
		}
		chain.doFilter(request, response);
	}

}
