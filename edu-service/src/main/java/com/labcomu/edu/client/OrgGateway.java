package com.labcomu.edu.client;

import com.labcomu.edu.configuration.EduProperties;
import com.labcomu.edu.resource.Organization;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Component
@Validated
public class OrgGateway {
    private final String fetchOrganizationUrl;

    private final WebClient.Builder webClientBuilder;

    private static final Logger logger = LoggerFactory.getLogger(OrgGateway.class);

    public OrgGateway(final WebClient.Builder webClientBuilder,
            final EduProperties properties) {
        this.webClientBuilder = webClientBuilder;
        this.fetchOrganizationUrl = properties.getUrl().getFetchOrganizationDetails();
    }

    @CircuitBreaker(name="org_circuit", fallbackMethod = "fallback")
    public Organization getOrganization(@NotNull final String url) {
        return webClientBuilder.build()
                .get()
                .uri(fetchOrganizationUrl, url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Organization.class)
                .block();
    }

    private Organization fallback(Exception exception){
        logger.error("org-service não disponível. ["+ exception.getMessage()+"]");
        Organization organization = new Organization();
        organization.setName("");
        organization.setUrl("");
        organization.setResearchers(new ArrayList<>());

        return organization;
    }

}