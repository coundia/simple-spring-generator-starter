package com.pcoundia.config.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

@Slf4j
public class HeaderPropagationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Get the current request attributes
        if (HeaderPropagationControl.shouldPropagateHeaders()) {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (requestAttributes != null) {
                // Get the current request headers
                HttpHeaders currentHeaders = new HttpHeaders();
                requestAttributes.getRequest().getHeaderNames().asIterator()
                        .forEachRemaining(headerName -> {
                            String headerValue = requestAttributes.getRequest().getHeader(headerName);
//                            if (headerName.equalsIgnoreCase("X-User")) {
//                                log.info("X-User applicant Value Propagated {}", headerValue);
//                            }
                            currentHeaders.add(headerName, headerValue);
                        });

                // Add the current request headers to the RestTemplate request
                request.getHeaders().addAll(currentHeaders);
            }
        }


        // Continue with the RestTemplate request execution
        return execution.execute(request, body);
    }
}