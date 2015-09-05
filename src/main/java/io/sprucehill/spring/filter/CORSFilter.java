package io.sprucehill.spring.filter;

import io.sprucehill.spring.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A OncePerRequestFilter implementation that will add CORS relevant header to any request defined by the configuration.
 *
 * @author Michael Duergner
 */
@Component
@ConditionalOnProperty(value = "io.sprucehill.spring.filter.cors.enable",havingValue = "true")
public class CORSFilter extends OncePerRequestFilter {

    public static class HttpHeaders {
        static final String ACCEPT = "Accept";
        static final String ACCEPT_LANGUAGE = "Accept-Language";
        static final String CONTENT_ENCODING = "Content-Encoding";
        static final String ETAG = "Etag";
        static final String REFERER = "Referer";
        static final String USER_AGENT = "User-Agent";
        static final String AUTHORIZATION = "Authorization";
        static final String CONTENT_LENGTH = "Content-Length";
        static final String CONTENT_TYPE = "Content-Type";
        static final String ORIGIN = "Origin";
        static final String X_REQUESTED_WITH = "X-Requested-With";
        static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
        static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
            static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
        static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    }

    @Value(value = "${io.sprucehill.spring.filter.cors.header.maxAge}")
    String maxAge;

    String allowedMethods;

    String allowedHeaders;

    Set<PathMatcher> includePath;

    Set<PathMatcher> excludePath;

    Set<String> includeOrigin;

    Set<String> excludeOrigin;

    /**
     * Set the allowed methods
     *
     * @param allowedMethods    A comma separated list of allowed request methods
     */
    @Value(value = "#{'${io.sprucehill.spring.filter.cors.methods}'.split(',')}")
    public void setAllowedMethods(Set<String> allowedMethods) {
        this.allowedMethods = String.join(", ",allowedMethods.stream().map(method -> method.toUpperCase()).collect(Collectors.toSet()));
    }

    /**
     * Set the allowed headers
     *
     * @param allowedHeaders    A set of allowed request headers
     */
    @Value(value = "#{'${io.sprucehill.spring.filter.cors.headers}'.split(',')}")
    public void setAllowedHeaders(Set<String> allowedHeaders) {
        this.allowedHeaders = String.join(", ",allowedHeaders.stream().map(method -> method.toUpperCase()).collect(Collectors.toSet()));
    }

    /**
     * Set the included paths; an empty list will apply it to all paths except the ones specified by the 'excludePath'
     *
     * @param includePath    A set of paths to apply this filter on
     */
    @Value(value = "#{'${io.sprucehill.spring.filter.cors.path.include:}'.split(',')}")
    public void setIncludePath(Set<String> includePath) {
        if (1 == includePath.size() && includePath.stream().findFirst().get().isEmpty()) {
            this.includePath = new HashSet<>();
        }
        else {
            this.includePath = includePath.stream().map(AntPathMatcher::new).collect(Collectors.toSet());
        }
    }

    /**
     * Set the excluded paths; excluded paths have precedence over included ones
     *
     * @param excludePath    A set of paths to not apply this filter on
     */
    @Value(value = "#{'${io.sprucehill.spring.filter.cors.path.exclude:}'.split(',')}")
    public void setExcludePath(Set<String> excludePath) {
        if (1 == excludePath.size() && excludePath.stream().findFirst().get().isEmpty()) {
            this.excludePath = new HashSet<>();
        }
        else {
            this.excludePath = excludePath.stream().map(AntPathMatcher::new).collect(Collectors.toSet());
        }
    }

    /**
     * Set the included origins; an empty list will apply the filter for all origins; '*.' indicates all subdomains for the specified host
     *
     * @param includeOrigin    A set of origins to apply this filter on
     */
    @Value(value = "#{'${io.sprucehill.spring.filter.cors.origin.include:}'.split(',')}")
    public void setIncludeOrigin(Set<String> includeOrigin) {
        if (1 == includeOrigin.size() && includeOrigin.stream().findFirst().get().isEmpty()) {
            this.includeOrigin = new HashSet<>();
        }
        else {
            this.includeOrigin = includeOrigin.stream().map(origin -> origin.trim().toLowerCase()).collect(Collectors.toSet());
        }
    }

    /**
     * Set the excluded origins; excluded origins have precedence over included ones; '*.' indicates all subdomains for the specified host
     *
     * @param excludeOrigin    A comma separeted list
     */
    @Value(value = "#{'${io.sprucehill.spring.filter.cors.origin.exclude:}'.split(',')}")
    public void setExcludeOrigin(Set<String> excludeOrigin) {
        if (1 == excludeOrigin.size() && excludeOrigin.stream().findFirst().get().isEmpty()) {
            this.excludeOrigin = new HashSet<>();
        }
        else {
            this.excludeOrigin = excludeOrigin.stream().map(origin -> origin.trim().toLowerCase()).collect(Collectors.toSet());
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, javax.servlet.FilterChain filterChain) throws ServletException, IOException {
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, request.getHeader(HttpHeaders.ORIGIN));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, allowedMethods);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, maxAge);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, allowedHeaders);
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !shouldFilterByPath(request) || !shouldFilterByOrigin(request);
    }

    boolean shouldFilterByPath(HttpServletRequest request) {
        return (0 == includePath.size() || matchPath(includePath, request)) && (0 == excludePath.size() || !matchPath(excludePath, request));
    }

    boolean shouldFilterByOrigin(HttpServletRequest request) {
        return (0 == includeOrigin.size() || matchOrigin(includeOrigin,request)) || (0 == excludeOrigin.size() || !matchOrigin(excludeOrigin,request));
    }

    boolean matchPath(Set<PathMatcher> matchers, HttpServletRequest request) {
        for (PathMatcher matcher : matchers) {
            if (matcher.isPattern(request.getPathInfo())) {
                return true;
            }
        }
        return false;
    }

    boolean matchOrigin(Set<String> origins, HttpServletRequest request) {
        String requestOrigin = request.getHeader(HttpHeaders.ORIGIN);
        if (null == requestOrigin) {
            return false;
        }
        requestOrigin = requestOrigin.trim().toLowerCase();
        for (String origin : origins) {
            if (origin.startsWith("*") || origin.endsWith(requestOrigin)) {
                return true;
            }
            else if (origin.equals(requestOrigin)) {
                return true;
            }
        }
        return false;
    }
}
