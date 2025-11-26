package uk.co.emcreations.energycoop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.co.emcreations.energycoop.dto.VensysMeanData;
import uk.co.emcreations.energycoop.dto.VensysPerformanceData;
import uk.co.emcreations.energycoop.service.impl.GraigFathaStatsServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GraigFathaStatsController.class)
class GraigFathaStatsControllerTest {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static String baseURL = "/api/v1/graigFatha/stats";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    GraigFathaStatsServiceImpl service;

    @Test
    @DisplayName("GET /energyYield returns 200 OK")
    void testGetEnergyYield() throws Exception {
        var expectedEnergyYield = VensysMeanData.builder().value(100).build();

        when(service.getMeanEnergyYield()).thenReturn(Optional.ofNullable(expectedEnergyYield));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/energyYield").with(oidcLogin()))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        VensysMeanData actualEnergyYield = objectMapper.readValue(json, VensysMeanData.class);

        assertEquals(expectedEnergyYield, actualEnergyYield);
    }

    @Test
    @DisplayName("GET /yesterdayPerformance returns 200 OK")
    void testYesterdayPerformance() throws Exception {
        var expectedYesterdayPerformance = VensysPerformanceData.builder().powerAvg(200).build();

        when(service.getYesterdayPerformance()).thenReturn(expectedYesterdayPerformance);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/yesterdayPerformance").with(oidcLogin()))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        VensysPerformanceData actualEnergyYield = objectMapper.readValue(json, VensysPerformanceData.class);

        assertEquals(expectedYesterdayPerformance, actualEnergyYield);
    }

    @Test
    @DisplayName("GET /energyYield throws error if service throws")
    void testGetEnergyYield_serviceThrows() {
        when(service.getMeanEnergyYield()).thenThrow(new RuntimeException("fail"));

        assertThrows(ServletException.class, () -> {
            mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/energyYield").with(oidcLogin()))
                    .andExpect(status().is4xxClientError())
                    .andReturn();
        });
    }

    @Test
    @DisplayName("GET /energyYield returns 200 OK with null response")
    void testGetEnergyYield_nullResponse() throws Exception {
        when(service.getMeanEnergyYield()).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/energyYield").with(oidcLogin()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /yesterdayPerformance throws error if service throws")
    void testYesterdayPerformance_serviceThrows() throws Exception {
        when(service.getYesterdayPerformance()).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/todaySavings").with(oidcLogin()))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    @DisplayName("GET /yesterdayPerformance returns 200 OK with null response")
    void testYesterdayPerformance_nullResponse() throws Exception {
        when(service.getYesterdayPerformance()).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/yesterdayPerformance").with(oidcLogin()))
                .andExpect(status().isOk());
    }
}