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
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.validation.constraints.NotNull;
import java.time.Duration;
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

    private Organization getEmpityOrg(){
        Organization organization = new Organization();
        organization.setName("");
        organization.setUrl("");
        organization.setResearchers(new ArrayList<>());
        return organization;
    }

    @CircuitBreaker(name="org_circuit", fallbackMethod = "fallback")
    public Organization getOrganization(@NotNull final String url) {
        return webClientBuilder.build()
                .get()
                .uri(fetchOrganizationUrl, url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Organization.class)
                .timeout(Duration.ofSeconds(4))
                .block();
    }

    private Organization fallback(Exception exception){
        if (exception instanceof WebClientResponseException.ServiceUnavailable){
            logger.error("org-service não disponível. ["+ exception.toString()+"]");
        }else {
            logger.error("tempo esgotado para edu-service. ["+ exception.toString()+"]");
        }
        return getEmpityOrg();
    }

    private Organization fallback_timeout(Exception exception){
        logger.error("Tempo esgotado para org-service. Requisição durou mais de três segundos.");
        return getEmpityOrg();
    }

}