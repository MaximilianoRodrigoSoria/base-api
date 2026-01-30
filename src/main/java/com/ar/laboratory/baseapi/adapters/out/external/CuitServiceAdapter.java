package com.ar.laboratory.baseapi.adapters.out.external;

import com.ar.laboratory.baseapi.domain.ports.out.CuitServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adapter for CUIT service using WireMock.
 */
@Component
public class CuitServiceAdapter implements CuitServicePort {

    private static final Logger logger = LoggerFactory.getLogger(CuitServiceAdapter.class);
    
    private final RestClient restClient;
    private final String wiremockUrl;

    public CuitServiceAdapter(@Value("${wiremock.url}") String wiremockUrl, RestClient.Builder restClientBuilder) {
        this.wiremockUrl = wiremockUrl;
        this.restClient = restClientBuilder.baseUrl(wiremockUrl).build();
    }

    @Override
    public String getCuit(String dni, String genero) {
        logger.info("Getting CUIT for DNI: {} and gender: {}", dni, genero);
        
        try {
            CuitResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/cuit")
                            .queryParam("dni", dni)
                            .queryParam("genero", genero)
                            .build())
                    .retrieve()
                    .body(CuitResponse.class);
            
            String cuit = response != null ? response.cuit() : calculateCuitLocally(dni, genero);
            logger.info("CUIT obtained: {}", cuit);
            return cuit;
        } catch (Exception e) {
            logger.error("Error calling CUIT service, calculating locally", e);
            return calculateCuitLocally(dni, genero);
        }
    }
    
    private String calculateCuitLocally(String dni, String genero) {
        return "H".equalsIgnoreCase(genero) ? "20-" + dni + "-7" : "27-" + dni + "-6";
    }
    
    private record CuitResponse(String cuit, String dni, String genero) {}
}
