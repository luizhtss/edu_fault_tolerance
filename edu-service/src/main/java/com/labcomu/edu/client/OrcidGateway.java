package com.labcomu.edu.client;

import com.labcomu.edu.configuration.EduProperties;
import com.labcomu.edu.resource.Researcher;
import io.github.resilience4j.retry.annotation.Retry;
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
public class OrcidGateway {
    private final String fetchResearcherUrl;

    private final WebClient.Builder webClientBuilder;

    private static final Logger logger = LoggerFactory.getLogger(OrcidGateway.class);


    public OrcidGateway(final WebClient.Builder webClientBuilder,
            final EduProperties properties) {
        this.webClientBuilder = webClientBuilder;
        this.fetchResearcherUrl = properties.getUrl().getFetchResearcherDetails();

    }

    @Retry(name="orcid_retry", fallbackMethod = "fallback")
    public Researcher getResearcher(@NotNull final String orcid) {
        return webClientBuilder.build()
                .get()
                .uri(fetchResearcherUrl, orcid)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Researcher.class)
                .block();
    }

    private Researcher fallback(Exception exception){
        logger.error("orcid-service falhou! ["+ exception.getMessage()+"]");
        return null;
    }
}
