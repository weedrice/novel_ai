package com.jwyoo.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.entity.Project;
import com.jwyoo.api.entity.Script;
import com.jwyoo.api.entity.User;
import com.jwyoo.api.repository.ProjectRepository;
import com.jwyoo.api.repository.ScriptRepository;
import com.jwyoo.api.repository.UserRepository;
import com.jwyoo.api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ScriptIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ScriptRepository scriptRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private Project testProject;

    @BeforeEach
    void setUp() {
        // Create test user
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        authToken = jwtTokenProvider.generateToken(testUser.getUsername());

        // Create test project
        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setOwner(testUser);
        testProject = projectRepository.save(testProject);
    }

    @Test
    @DisplayName("Integration Test: Upload Script")
    void uploadScript_Integration_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("title", "Test Script");
        request.put("content", "Script content");
        request.put("formatHint", "novel");

        mockMvc.perform(post("/scripts")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Script"))
                .andExpect(jsonPath("$.status").value("uploaded"));
    }

    @Test
    @DisplayName("Integration Test: Get All Scripts")
    void getAllScripts_Integration_Success() throws Exception {
        // Create test scripts
        Script script1 = Script.builder()
                .title("Script 1")
                .content("Content 1")
                .status("uploaded")
                .project(testProject)
                .build();
        scriptRepository.save(script1);

        Script script2 = Script.builder()
                .title("Script 2")
                .content("Content 2")
                .status("analyzed")
                .project(testProject)
                .build();
        scriptRepository.save(script2);

        mockMvc.perform(get("/scripts")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[1].title").exists());
    }

    @Test
    @DisplayName("Integration Test: Get Script By ID")
    void getScriptById_Integration_Success() throws Exception {
        Script script = Script.builder()
                .title("Query Test Script")
                .content("Query content")
                .status("uploaded")
                .project(testProject)
                .build();
        script = scriptRepository.save(script);

        mockMvc.perform(get("/scripts/{id}", script.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Query Test Script"))
                .andExpect(jsonPath("$.status").value("uploaded"));
    }

    @Test
    @DisplayName("Integration Test: Get Scripts By Status")
    void getScriptsByStatus_Integration_Success() throws Exception {
        Script script1 = Script.builder()
                .title("Uploaded Script")
                .content("Content")
                .status("uploaded")
                .project(testProject)
                .build();
        scriptRepository.save(script1);

        Script script2 = Script.builder()
                .title("Analyzed Script")
                .content("Content")
                .status("analyzed")
                .project(testProject)
                .build();
        scriptRepository.save(script2);

        mockMvc.perform(get("/scripts/status/{status}", "uploaded")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Integration Test: Delete Script")
    void deleteScript_Integration_Success() throws Exception {
        Script script = Script.builder()
                .title("To Delete Script")
                .content("Delete content")
                .status("uploaded")
                .project(testProject)
                .build();
        script = scriptRepository.save(script);

        Long scriptId = script.getId();

        mockMvc.perform(delete("/scripts/{id}", scriptId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/scripts/{id}", scriptId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration Test: Search Scripts")
    void searchScripts_Integration_Success() throws Exception {
        Script script1 = Script.builder()
                .title("Fantasy Novel Script")
                .content("Fantasy content")
                .status("uploaded")
                .project(testProject)
                .build();
        scriptRepository.save(script1);

        Script script2 = Script.builder()
                .title("SF Novel")
                .content("SF content")
                .status("uploaded")
                .project(testProject)
                .build();
        scriptRepository.save(script2);

        mockMvc.perform(get("/scripts/search")
                        .header("Authorization", "Bearer " + authToken)
                        .param("keyword", "Fantasy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Integration Test: Get Analysis Result")
    void getAnalysisResult_Integration_Success() throws Exception {
        Script script = Script.builder()
                .title("Analyzed Script")
                .content("Content")
                .status("analyzed")
                .analysisResult("{\"characters\": [], \"scenes\": []}")
                .project(testProject)
                .build();
        script = scriptRepository.save(script);

        mockMvc.perform(get("/scripts/{id}/analysis", script.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.characters").exists());
    }

    @Test
    @DisplayName("Integration Test: Access Script Without Auth - Failure")
    void accessScriptWithoutAuth_Integration_Failure() throws Exception {
        mockMvc.perform(get("/scripts"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Integration Test: Get Non-existent Script - Failure")
    void getScriptById_NotFound_Failure() throws Exception {
        mockMvc.perform(get("/scripts/{id}", 99999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration Test: Full Flow - Upload, Query, Delete")
    void fullFlow_Integration_Success() throws Exception {
        // 1. Upload
        Map<String, String> uploadRequest = new HashMap<>();
        uploadRequest.put("title", "Full Flow Script");
        uploadRequest.put("content", "Script content");

        String createResponse = mockMvc.perform(post("/scripts")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uploadRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Script createdScript = objectMapper.readValue(createResponse, Script.class);
        Long scriptId = createdScript.getId();

        // 2. Query
        mockMvc.perform(get("/scripts/{id}", scriptId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Full Flow Script"));

        // 3. Delete
        mockMvc.perform(delete("/scripts/{id}", scriptId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // 4. Verify deletion
        mockMvc.perform(get("/scripts/{id}", scriptId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
}
