package com.ar.laboratory.baseapi.domain.ports.out;

/**
 * Output port for CUIT service operations.
 */
public interface CuitServicePort {
    
    /**
     * Gets CUIT based on DNI and gender.
     *
     * @param dni DNI number
     * @param genero Gender (H for male, M for female)
     * @return calculated CUIT
     */
    String getCuit(String dni, String genero);
}
