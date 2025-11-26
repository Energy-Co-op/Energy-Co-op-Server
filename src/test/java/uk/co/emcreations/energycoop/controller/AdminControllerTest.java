package uk.co.emcreations.energycoop.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.co.emcreations.energycoop.entity.Alert;
import uk.co.emcreations.energycoop.entity.SavingsRate;
import uk.co.emcreations.energycoop.model.Site;
import uk.co.emcreations.energycoop.service.AlertService;
import uk.co.emcreations.energycoop.service.impl.SavingsRateServiceImpl;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(AdminController.class)
class AdminControllerTest {
    private static final String BASE_URL = "/api/v1/admin";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SavingsRateServiceImpl savingsRateService;

    @MockitoBean
    private AlertService alertService;

    @Nested
    @DisplayName("setSavingsRate tests")
    class SetSavingsRateTests {
        private final LocalDate TEST_DATE = LocalDate.of(2024, 1, 1);
        private final String TEST_DATE_STR = "2024-01-01";
        private final double TEST_RATE = 5.67;
        private final String TEST_USER_ID = "test-user";

        @Disabled
        @Test
        @WithMockUser
        @DisplayName("POST /savings-rate returns 200 OK for Graig Fatha")
        void testSetSavingsRateGraigFatha() throws Exception {
            var expectedRate = SavingsRate.builder()
                    .site(Site.GRAIG_FATHA)
                    .effectiveDate(TEST_DATE)
                    .ratePerKWH(TEST_RATE)
                    .lastUpdatedByUser(TEST_USER_ID)
                    .build();

            when(savingsRateService.setSavingsRateForDate(eq(Site.GRAIG_FATHA), eq(TEST_DATE), eq(TEST_RATE), eq(TEST_USER_ID)))
                    .thenReturn(expectedRate);

            var requestJson = """
                    {
                        "site": "GRAIG_FATHA",
                        "effectiveDate": "%s",
                        "ratePerKWH": %s
                    }""".formatted(TEST_DATE_STR, TEST_RATE);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                    .post(BASE_URL + "/savings-rate")
                    .contentType(APPLICATION_JSON)
                    .content(requestJson)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn();

            String json = result.getResponse().getContentAsString();
            assertTrue(json.contains(String.valueOf(expectedRate.getRatePerKWH())));
        }

        @Disabled
        @Test
        @WithMockUser(authorities = "set:savings-rate")
        @DisplayName("POST /savings-rate returns 200 OK for Kirk Hill")
        void testSetSavingsRateKirkHill() throws Exception {
            var expectedRate = SavingsRate.builder()
                    .site(Site.KIRK_HILL)
                    .effectiveDate(TEST_DATE)
                    .ratePerKWH(TEST_RATE)
                    .build();

            when(savingsRateService.setSavingsRateForDate(eq(Site.KIRK_HILL), eq(TEST_DATE), eq(TEST_RATE), eq(TEST_USER_ID)))
                    .thenReturn(expectedRate);

            var requestJson = """
                    {
                        "site": "KIRK_HILL",
                        "effectiveDate": "%s",
                        "ratePerKWH": %s
                    }""".formatted(TEST_DATE_STR, TEST_RATE);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                    .post(BASE_URL + "/savings-rate")
                    .contentType(APPLICATION_JSON)
                    .content(requestJson)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn();

            String json = result.getResponse().getContentAsString();
            assertTrue(json.contains(String.valueOf(expectedRate.getRatePerKWH())));
        }

        @Disabled
        @Test
        @WithMockUser
        @DisplayName("POST /savings-rate returns 200 OK for Derril Water")
        void testSetSavingsRateDerrilWater() throws Exception {
            var expectedRate = SavingsRate.builder()
                    .site(Site.DERRIL_WATER)
                    .effectiveDate(TEST_DATE)
                    .ratePerKWH(TEST_RATE)
                    .build();

            when(savingsRateService.setSavingsRateForDate(eq(Site.DERRIL_WATER), eq(TEST_DATE), eq(TEST_RATE), eq(TEST_USER_ID)))
                    .thenReturn(expectedRate);

            var requestJson = """
                    {
                        "site": "DERRIL_WATER",
                        "effectiveDate": "%s",
                        "ratePerKWH": %s
                    }""".formatted(TEST_DATE_STR, TEST_RATE);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                    .post(BASE_URL + "/savings-rate")
                    .contentType(APPLICATION_JSON)
                    .content(requestJson)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn();

            String json = result.getResponse().getContentAsString();
            assertTrue(json.contains(String.valueOf(expectedRate.getRatePerKWH())));
        }

        @Test
        @WithMockUser
        @DisplayName("POST /savings-rate returns 400 BAD REQUEST for invalid site")
        void testSetSavingsRateInvalidSite() throws Exception {
            var invalidJson = """
                    {
                        "site": "INVALID_SITE",
                        "effectiveDate": "2024-01-01",
                        "ratePerKWH": 5.67
                    }""";

            mockMvc.perform(MockMvcRequestBuilders
                    .post(BASE_URL + "/savings-rate")
                    .contentType(APPLICATION_JSON)
                    .content(invalidJson)
                    .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("POST /savings-rate returns 400 BAD REQUEST for invalid date format")
        void testSetSavingsRateInvalidDate() throws Exception {
            var invalidJson = """
                    {
                        "site": "GRAIG_FATHA",
                        "effectiveDate": "invalid-date",
                        "ratePerKWH": 5.67
                    }""";

            mockMvc.perform(MockMvcRequestBuilders
                    .post(BASE_URL + "/savings-rate")
                    .contentType(APPLICATION_JSON)
                    .content(invalidJson)
                    .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("POST /savings-rate returns 400 BAD REQUEST for invalid rate")
        void testSetSavingsRateInvalidRate() throws Exception {
            var invalidJson = """
                    {
                        "site": "GRAIG_FATHA",
                        "effectiveDate": "2024-01-01",
                        "ratePerKWH": "invalid-rate"
                    }""";

            mockMvc.perform(MockMvcRequestBuilders
                    .post(BASE_URL + "/savings-rate")
                    .contentType(APPLICATION_JSON)
                    .content(invalidJson)
                    .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /savings-rate returns 403 FORBIDDEN without login")
        void testSetSavingsRateUnauthorized() throws Exception {
            var requestJson = """
                    {
                        "site": "GRAIG_FATHA",
                        "effectiveDate": "%s",
                        "ratePerKWH": %s
                    }""".formatted(TEST_DATE_STR, TEST_RATE);

            mockMvc.perform(MockMvcRequestBuilders
                    .post(BASE_URL + "/savings-rate")
                    .contentType(APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("getAlerts tests")
    class GetAlertsTests {
        @Test
        @WithMockUser(authorities = "read:alerts")
        @DisplayName("GET /alerts/{site} returns 200 OK and alerts for Graig Fatha")
        void getAlerts_returnsAlertsForGraigFatha() throws Exception {
            var alerts = List.of(
                Alert.builder()
                    .site(Site.GRAIG_FATHA)
                    .message("Test alert 1")
                    .build(),
                Alert.builder()
                    .site(Site.GRAIG_FATHA)
                    .message("Test alert 2")
                    .build()
            );
            when(alertService.getLatestAlerts(Site.GRAIG_FATHA)).thenReturn(alerts);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                    .get(BASE_URL + "/alerts/GRAIG_FATHA")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn();

            String json = result.getResponse().getContentAsString();
            assertTrue(json.contains("Test alert 1"));
            assertTrue(json.contains("Test alert 2"));
        }

        @Test
        @WithMockUser(authorities = "read:alerts")
        @DisplayName("GET /alerts/{site} returns 200 OK and alerts for Kirk Hill")
        void getAlerts_returnsAlertsForKirkHill() throws Exception {
            var alerts = List.of(
                Alert.builder()
                    .site(Site.KIRK_HILL)
                    .message("Kirk Hill alert")
                    .build()
            );
            when(alertService.getLatestAlerts(Site.KIRK_HILL)).thenReturn(alerts);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                    .get(BASE_URL + "/alerts/KIRK_HILL")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn();

            String json = result.getResponse().getContentAsString();
            assertTrue(json.contains("Kirk Hill alert"));
        }

        @Test
        @WithMockUser(authorities = "read:alerts")
        @DisplayName("GET /alerts/{site} returns 200 OK and alerts for Derril Water")
        void getAlerts_returnsAlertsForDerrilWater() throws Exception {
            var alerts = List.of(
                Alert.builder()
                    .site(Site.DERRIL_WATER)
                    .message("Derril Water alert")
                    .build()
            );
            when(alertService.getLatestAlerts(Site.DERRIL_WATER)).thenReturn(alerts);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                    .get(BASE_URL + "/alerts/DERRIL_WATER")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn();

            String json = result.getResponse().getContentAsString();
            assertTrue(json.contains("Derril Water alert"));
        }

        @Test
        @WithMockUser(authorities = "read:alerts")
        @DisplayName("GET /alerts/{site} returns 200 OK and empty list when no alerts")
        void getAlerts_returnsEmptyList_whenNoAlerts() throws Exception {
            when(alertService.getLatestAlerts(any(Site.class))).thenReturn(List.of());

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                    .get(BASE_URL + "/alerts/GRAIG_FATHA")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn();

            String json = result.getResponse().getContentAsString();
            assertEquals("[]", json, "Should return empty JSON array");
        }


        @Test
        @DisplayName("GET /alerts/{site} returns 302 FORBIDDEN without login")
        void getAlerts_returnsForbidden_withoutLogin() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                    .get(BASE_URL + "/alerts/GRAIG_FATHA")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @WithMockUser(authorities = "read:alerts")
        @DisplayName("GET /alerts/{site} returns 400 BAD REQUEST for invalid site")
        void getAlerts_returnsBadRequest_forInvalidSite() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                    .get(BASE_URL + "/alerts/INVALID_SITE")
                    .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }
}
