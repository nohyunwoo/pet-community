package com.example.community.service;

import com.example.community.dto.UserRequestDTO;
import com.example.community.entity.User;
import com.example.community.exception.CustomException;
import com.example.community.exception.ErrorCode;
import com.example.community.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공")
    void userRegister_success() {
        // given
        UserRequestDTO requestDTO = createValidRequestDTO();
        when(userRepository.existsByUserId(requestDTO.getUserId())).thenReturn(false);
        when(passwordEncoder.encode(requestDTO.getPassword())).thenReturn("encoded-password");

        // when
        userService.userRegister(requestDTO);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertEquals("encoded-password", savedUser.getPassword());
        assertNotNull(savedUser.getProfile());
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 불일치")
    void userRegister_passwordMismatch_throwsIllegalArgumentException() {
        // given
        UserRequestDTO requestDTO = createValidRequestDTO();
        requestDTO.setPasswordConfirm("different-password");

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.userRegister(requestDTO));

        // then
        assertEquals("비밀번호와 비밀번호 확인이 일치하지 않습니다.", exception.getMessage());
        verify(userRepository, never()).existsByUserId(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 아이디")
    void userRegister_duplicateUserId_throwsIllegalArgumentException() {
        // given
        UserRequestDTO requestDTO = createValidRequestDTO();
        when(userRepository.existsByUserId(requestDTO.getUserId())).thenReturn(true);

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.userRegister(requestDTO));

        // then
        assertEquals("이미 사용중인 아이디입니다.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 요청 DTO가 null")
    void userRegister_nullRequest_throwsNullPointerException() {
        // given
        UserRequestDTO requestDTO = null;

        // when
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> userService.userRegister(requestDTO));

        // then
        assertNotNull(exception);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호가 null")
    void userRegister_nullPassword_throwsNullPointerException() {
        // given
        UserRequestDTO requestDTO = createValidRequestDTO();
        requestDTO.setPassword(null);

        // when
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> userService.userRegister(requestDTO));

        // then
        assertNotNull(exception);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - existsByUserId 조회 중 DB 에러")
    void userRegister_existsByUserId_dbError_propagates() {
        // given
        UserRequestDTO requestDTO = createValidRequestDTO();
        when(userRepository.existsByUserId(requestDTO.getUserId()))
                .thenThrow(new RuntimeException("DB read error"));

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.userRegister(requestDTO));

        // then
        assertEquals("DB read error", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - save 중 CustomException 발생")
    void userRegister_saveThrowsCustomException_propagates() {
        // given
        UserRequestDTO requestDTO = createValidRequestDTO();
        when(userRepository.existsByUserId(requestDTO.getUserId())).thenReturn(false);
        when(passwordEncoder.encode(requestDTO.getPassword())).thenReturn("encoded-password");
        doThrow(new CustomException(ErrorCode.SERVER_ERROR)).when(userRepository).save(any(User.class));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.userRegister(requestDTO));

        // then
        assertEquals(ErrorCode.SERVER_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자 조회 성공")
    void existUserId_success() {
        // given
        Long id = 1L;
        User user = User.builder().id(id).username("tester").build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // when
        User result = userService.existUserId(id);

        // then
        assertEquals(id, result.getId());
        assertEquals("tester", result.getUsername());
    }

    @Test
    @DisplayName("사용자 조회 실패 - 존재하지 않는 사용자")
    void existUserId_notFound_throwsIllegalArgumentException() {
        // given
        Long id = 99L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.existUserId(id));

        // then
        assertEquals("존재하지 않는 사용자 입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사용자 조회 실패 - null id 전달")
    void existUserId_nullId_throwsIllegalArgumentException() {
        // given
        when(userRepository.findById(isNull()))
                .thenThrow(new IllegalArgumentException("The given id must not be null"));

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.existUserId(null));

        // then
        assertEquals("The given id must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("사용자 조회 실패 - DB 에러(CustomException) 전파")
    void existUserId_repositoryThrowsCustomException_propagates() {
        // given
        Long id = 1L;
        when(userRepository.findById(id)).thenThrow(new CustomException(ErrorCode.SERVER_ERROR));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> userService.existUserId(id));

        // then
        assertEquals(ErrorCode.SERVER_ERROR, exception.getErrorCode());
    }

    private UserRequestDTO createValidRequestDTO() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setUsername("tester");
        dto.setUserId("tester01");
        dto.setPassword("password123");
        dto.setPasswordConfirm("password123");
        dto.setSex("M");
        dto.setUserDate(LocalDate.of(2000, 1, 1));
        return dto;
    }
}
