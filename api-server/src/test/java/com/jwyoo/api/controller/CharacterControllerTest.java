package com.jwyoo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwyoo.api.dto.CharacterDto;
import com.jwyoo.api.dto.SpeakingProfileDto;
import com.jwyoo.api.entity.Character;
import com.jwyoo.api.exception.ResourceNotFoundException;
import com.jwyoo.api.service.CharacterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CharacterController 테스트
 * 캐릭터 CRUD 및 말투 프로필 관리 기능 테스트
 */
@WebMvcTest(controllers = CharacterController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CharacterService characterService;

    @MockBean
    private com.jwyoo.api.security.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.jwyoo.api.security.JwtTokenProvider jwtTokenProvider;

    private Character testCharacter;
    private CharacterDto testCharacterDto;

    @BeforeEach
    void setUp() {
        testCharacter = Character.builder()
                .id(1L)
                .characterId("char.seha")
                .name("세하")
                .description("밝고 긍정적인 성격의 주인공")
                .personality("밝고 낙천적이며, 친구들에게 헌신적이다")
                .speakingStyle("반말, 친근한 말투")
                .vocabulary("야, 진짜, 대박, 완전")
                .toneKeywords("친근함, 활기찬, 긍정적")
                .build();

        testCharacterDto = CharacterDto.builder()
                .id(1L)
                .characterId("char.seha")
                .name("세하")
                .description("밝고 긍정적인 성격의 주인공")
                .personality("밝고 낙천적이며, 친구들에게 헌신적이다")
                .build();
    }

    @Test
    @DisplayName("캐릭터 목록 조회 성공")
    void getAllCharacters_Success() throws Exception {
        // given
        List<Character> characters = List.of(testCharacter);
        when(characterService.getAllCharacters()).thenReturn(characters);

        // when & then
        mockMvc.perform(get("/characters")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("세하"))
                .andExpect(jsonPath("$[0].characterId").value("char.seha"));

        verify(characterService, times(1)).getAllCharacters();
    }

    @Test
    @DisplayName("캐릭터 단건 조회 성공")
    void getCharacterById_Success() throws Exception {
        // given
        when(characterService.getCharacterById(1L)).thenReturn(testCharacter);

        // when & then
        mockMvc.perform(get("/characters/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("세하"))
                .andExpect(jsonPath("$.personality").isNotEmpty());

        verify(characterService, times(1)).getCharacterById(1L);
    }

    @Test
    @DisplayName("캐릭터 조회 실패 - 존재하지 않는 ID")
    void getCharacterById_Failure_NotFound() throws Exception {
        // given
        when(characterService.getCharacterById(999L))
                .thenThrow(new ResourceNotFoundException("캐릭터", 999L));

        // when & then
        mockMvc.perform(get("/characters/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("캐릭터 생성 성공")
    void createCharacter_Success() throws Exception {
        // given
        when(characterService.createCharacter(any(Character.class))).thenReturn(testCharacter);

        // when & then
        mockMvc.perform(post("/characters")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCharacterDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("세하"))
                .andExpect(jsonPath("$.characterId").value("char.seha"));

        verify(characterService, times(1)).createCharacter(any(Character.class));
    }

    @Test
    @DisplayName("캐릭터 수정 성공")
    void updateCharacter_Success() throws Exception {
        // given
        Character updatedCharacter = Character.builder()
                .id(1L)
                .characterId("char.seha")
                .name("세하")
                .description("수정된 설명")
                .personality("밝고 낙천적이며, 친구들에게 헌신적이다")
                .build();

        when(characterService.updateCharacter(anyLong(), any(Character.class)))
                .thenReturn(updatedCharacter);

        CharacterDto updateDto = CharacterDto.builder()
                .characterId("char.seha")
                .name("세하")
                .description("수정된 설명")
                .personality("밝고 낙천적이며, 친구들에게 헌신적이다")
                .build();

        // when & then
        mockMvc.perform(put("/characters/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("수정된 설명"));

        verify(characterService, times(1)).updateCharacter(anyLong(), any(Character.class));
    }

    @Test
    @DisplayName("캐릭터 삭제 성공")
    void deleteCharacter_Success() throws Exception {
        // given
        doNothing().when(characterService).deleteCharacter(1L);

        // when & then
        mockMvc.perform(delete("/characters/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(characterService, times(1)).deleteCharacter(1L);
    }

    @Test
    @DisplayName("말투 프로필 조회 성공")
    void getSpeakingProfile_Success() throws Exception {
        // given
        when(characterService.getCharacterById(1L)).thenReturn(testCharacter);

        // when & then
        mockMvc.perform(get("/characters/1/speaking-profile")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.speakingStyle").value("반말, 친근한 말투"))
                .andExpect(jsonPath("$.vocabulary").value("야, 진짜, 대박, 완전"))
                .andExpect(jsonPath("$.toneKeywords").value("친근함, 활기찬, 긍정적"));

        verify(characterService, times(1)).getCharacterById(1L);
    }

    @Test
    @DisplayName("말투 프로필 업데이트 성공")
    void updateSpeakingProfile_Success() throws Exception {
        // given
        Character updatedCharacter = Character.builder()
                .id(1L)
                .characterId("char.seha")
                .name("세하")
                .speakingStyle("수정된 말투")
                .vocabulary("수정된 어휘")
                .toneKeywords("수정된 톤")
                .build();

        when(characterService.updateSpeakingProfile(anyLong(), any(Character.class)))
                .thenReturn(updatedCharacter);

        SpeakingProfileDto profileDto = new SpeakingProfileDto(
                "수정된 말투",
                "수정된 어휘",
                "수정된 톤",
                null,
                null,
                null
        );

        // when & then
        mockMvc.perform(put("/characters/1/speaking-profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.speakingStyle").value("수정된 말투"))
                .andExpect(jsonPath("$.vocabulary").value("수정된 어휘"))
                .andExpect(jsonPath("$.toneKeywords").value("수정된 톤"));

        verify(characterService, times(1)).updateSpeakingProfile(anyLong(), any(Character.class));
    }

    @Test
    @DisplayName("빈 캐릭터 목록 조회")
    void getAllCharacters_EmptyList() throws Exception {
        // given
        when(characterService.getAllCharacters()).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/characters")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
