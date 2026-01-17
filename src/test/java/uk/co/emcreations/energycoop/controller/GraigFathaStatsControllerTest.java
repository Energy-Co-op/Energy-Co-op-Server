package uk.co.emcreations.energycoop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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

    @Nested
    @DisplayName("GET /energyYield tests")
    class GetEnergyYieldTests {
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
    }

    @Nested
    @DisplayName("GET /yesterdayPerformance tests")
    class GetYesterdayPerformanceTests {
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

    @Nested
    @DisplayName("logPerformance endpoint tests")
    class LogPerformanceTests {
        @Test
        @DisplayName("POST /logPerformance/{from}/{to} returns 200 OK")
        void logPerformance_returnsOkStatus() throws Exception {
            var fromDate = LocalDate.of(2025, 11, 5);
            var toDate = LocalDate.of(2025, 11, 7);

            doNothing().when(service).logPerformance(fromDate, toDate);

            mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/logPerformance/2025-11-05/2025-11-07")
                    .with(oidcLogin()))
                    .andExpect(status().isOk())
                    .andReturn();

            verify(service).logPerformance(fromDate, toDate);
        }

        @Test
        @DisplayName("POST /logPerformance/{from}/{to} calls service with correct dates")
        void logPerformance_callsServiceWithCorrectDates() throws Exception {
            var fromDate = LocalDate.of(2025, 10, 1);
            var toDate = LocalDate.of(2025, 10, 31);

            doNothing().when(service).logPerformance(fromDate, toDate);

            mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/logPerformance/2025-10-01/2025-10-31")
                    .with(oidcLogin()))
                    .andExpect(status().isOk())
                    .andReturn();

            verify(service).logPerformance(fromDate, toDate);
        }

        @Test
        @DisplayName("POST /logPerformance/{from}/{to} returns 200 for single day")
        void logPerformance_singleDay_returnsOk() throws Exception {
            var date = LocalDate.of(2025, 11, 5);

            doNothing().when(service).logPerformance(date, date);

            mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/logPerformance/2025-11-05/2025-11-05")
                    .with(oidcLogin()))
                    .andExpect(status().isOk())
                    .andReturn();

            verify(service).logPerformance(date, date);
        }

        @Test
        @DisplayName("POST /logPerformance/{from}/{to} throws error if service throws")
        void logPerformance_serviceThrows_returnsError() {
            var fromDate = LocalDate.of(2025, 11, 5);
            var toDate = LocalDate.of(2025, 11, 7);

            doThrow(new RuntimeException("Service error")).when(service).logPerformance(fromDate, toDate);

            assertThrows(ServletException.class, () -> mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/logPerformance/2025-11-05/2025-11-07")
                    .with(oidcLogin()))
                    .andExpect(status().is5xxServerError())
                    .andReturn());

            verify(service).logPerformance(fromDate, toDate);
        }

        @Test
        @DisplayName("POST /logPerformance/{from}/{to} with invalid date format returns 400")
        void logPerformance_invalidDateFormat_returnsBadRequest() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/logPerformance/11-05-2025/11-07-2025")
                    .with(oidcLogin()))
                    .andExpect(status().isBadRequest())
                    .andReturn();
        }

        @Test
        @DisplayName("POST /logPerformance/{from}/{to} with date range spanning multiple months")
        void logPerformance_multipleMonths_returnsOk() throws Exception {
            var fromDate = LocalDate.of(2025, 9, 25);
            var toDate = LocalDate.of(2025, 11, 15);

            doNothing().when(service).logPerformance(fromDate, toDate);

            mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/logPerformance/2025-09-25/2025-11-15")
                    .with(oidcLogin()))
                    .andExpect(status().isOk())
                    .andReturn();

            verify(service).logPerformance(fromDate, toDate);
        }

        @Test
        @DisplayName("POST /logPerformance/{from}/{to} with start date after end date still calls service")
        void logPerformance_startAfterEnd_callsService() throws Exception {
            var fromDate = LocalDate.of(2025, 11, 7);
            var toDate = LocalDate.of(2025, 11, 5);

            doNothing().when(service).logPerformance(fromDate, toDate);

            mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/logPerformance/2025-11-07/2025-11-05")
                    .with(oidcLogin()))
                    .andExpect(status().isOk())
                    .andReturn();

            verify(service).logPerformance(fromDate, toDate);
        }

        @Test
        @DisplayName("POST /logPerformance/{from}/{to} with large date range")
        void logPerformance_largeRange_returnsOk() throws Exception {
            var fromDate = LocalDate.of(2025, 1, 1);
            var toDate = LocalDate.of(2025, 12, 31);

            doNothing().when(service).logPerformance(fromDate, toDate);

            mockMvc.perform(MockMvcRequestBuilders.get(baseURL + "/logPerformance/2025-01-01/2025-12-31")
                    .with(oidcLogin()))
                    .andExpect(status().isOk())
                    .andReturn();

            verify(service).logPerformance(fromDate, toDate);
        }
    }
}