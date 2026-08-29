package com.dadscare.backend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dadscare.backend.common.GlobalExceptionHandler;
import com.dadscare.backend.masterdata.MasterDataController;
import com.dadscare.backend.masterdata.MasterDataService;
import com.dadscare.backend.masterdata.ProductMasterDto;
import com.dadscare.backend.unlock.CommandType;
import com.dadscare.backend.unlock.UnlockRequestController;
import com.dadscare.backend.unlock.UnlockRequestDto;
import com.dadscare.backend.unlock.UnlockRequestService;
import com.dadscare.backend.unlock.UnlockRequestStatus;
import com.dadscare.backend.user.Role;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Proves {@code @PreAuthorize} actually enforces the role matrix end-to-end through the real
 * {@link JwtAuthFilter}/{@link JwtService} — not mocked out, since the whole point is verifying
 * a real JWT for each role gets the right {@code 200}/{@code 403}. Every other test in this
 * codebase is a pure Mockito service-unit test that never touches Spring Security at all, so
 * this is the one place that actually exercises {@link SecurityConfig}.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {MasterDataController.class, UnlockRequestController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class, GlobalExceptionHandler.class})
@TestPropertySource(
        properties = {
            "app.jwt.secret=test-only-secret-not-used-anywhere-real-1234567890",
            "app.jwt.access-token-ttl-minutes=60",
            "app.cors.allowed-origins=http://localhost:3000"
        })
class RoleAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private MasterDataService masterDataService;

    @MockBean
    private UnlockRequestService unlockRequestService;

    private String tokenFor(Role role) {
        return jwtService.issueAccessToken(1L, 7L, role.name(), false);
    }

    @Test
    void viewerIsForbiddenFromCreatingMasterData() throws Exception {
        mockMvc.perform(post("/api/v1/product-masters")
                        .header("Authorization", "Bearer " + tokenFor(Role.VIEWER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cement\",\"unit\":\"bags\",\"active\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void orgAdminCanCreateMasterData() throws Exception {
        when(masterDataService.createProduct(any())).thenReturn(new ProductMasterDto(1L, "Cement", "bags", true));

        mockMvc.perform(post("/api/v1/product-masters")
                        .header("Authorization", "Bearer " + tokenFor(Role.ORG_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cement\",\"unit\":\"bags\",\"active\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void siteManagerCanCreateMasterData() throws Exception {
        when(masterDataService.createProduct(any())).thenReturn(new ProductMasterDto(1L, "Cement", "bags", true));

        mockMvc.perform(post("/api/v1/product-masters")
                        .header("Authorization", "Bearer " + tokenFor(Role.SITE_MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cement\",\"unit\":\"bags\",\"active\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void viewerIsForbiddenFromOperatingAShutter() throws Exception {
        mockMvc.perform(post("/api/v1/devices/1/unlock-requests")
                        .header("Authorization", "Bearer " + tokenFor(Role.VIEWER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commandType\":\"UNLOCK\",\"stockLines\":[],\"truckEntries\":[],\"customFields\":[]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void operatorCanOperateAShutter() throws Exception {
        when(unlockRequestService.create(any(), any()))
                .thenReturn(new UnlockRequestDto(1L, 1L, CommandType.UNLOCK, UnlockRequestStatus.QUEUED, null, null, Instant.now()));

        mockMvc.perform(post("/api/v1/devices/1/unlock-requests")
                        .header("Authorization", "Bearer " + tokenFor(Role.OPERATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commandType\":\"UNLOCK\",\"stockLines\":[],\"truckEntries\":[],\"customFields\":[]}"))
                .andExpect(status().isOk());
    }

    @Test
    void requestsWithNoTokenAtAllAreUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/product-masters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cement\",\"unit\":\"bags\",\"active\":true}"))
                .andExpect(status().isForbidden());
    }
}
