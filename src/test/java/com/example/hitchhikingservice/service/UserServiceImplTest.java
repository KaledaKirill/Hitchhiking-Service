package com.example.hitchhikingservice.service;

import com.example.hitchhikingservice.cache.UserCache;
import com.example.hitchhikingservice.exception.EntityNotFoundException;
import com.example.hitchhikingservice.model.dto.request.UserRequestDto;
import com.example.hitchhikingservice.model.dto.response.UserResponseDto;
import com.example.hitchhikingservice.model.entity.Ride;
import com.example.hitchhikingservice.model.entity.User;
import com.example.hitchhikingservice.repository.UserRepository;
import com.example.hitchhikingservice.service.impl.UserServiceImpl;
import com.example.hitchhikingservice.service.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserCache userCache;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getAllUsers_shouldReturnMappedUserResponseDtoList() {
        User user1 = new User();
        User user2 = new User();

        List<User> users = List.of(user1, user2);

        UserResponseDto dto1 = new UserResponseDto(1L, "John", "john@mail.com", "12345");
        UserResponseDto dto2 = new UserResponseDto(2L, "Jane", "jane@mail.com", "67890");

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toUserResponseDto(user1)).thenReturn(dto1);
        when(userMapper.toUserResponseDto(user2)).thenReturn(dto2);

        List<UserResponseDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));
    }

    @Test
    void getUserById_shouldReturnUserFromCache_whenPresent() {
        Long id = 1L;
        User cachedUser = new User();
        UserResponseDto expectedDto = new UserResponseDto(id, "John", "john@mail.com", "12345");

        when(userCache.get(id)).thenReturn(cachedUser);
        when(userMapper.toUserResponseDto(cachedUser)).thenReturn(expectedDto);

        UserResponseDto actualDto = userService.getUserById(id);

        assertEquals(expectedDto, actualDto);
        verify(userRepository, never()).findById(any());
        verify(userCache, never()).put(any(), any());
    }

    @Test
    void getUserById_shouldReturnUserFromRepository_whenNotInCache() {
        Long id = 2L;
        User dbUser = new User();
        UserResponseDto expectedDto = new UserResponseDto(id, "Jane", "jane@mail.com", "67890");

        when(userCache.get(id)).thenReturn(null);
        when(userRepository.findById(id)).thenReturn(Optional.of(dbUser));
        when(userMapper.toUserResponseDto(dbUser)).thenReturn(expectedDto);

        UserResponseDto actualDto = userService.getUserById(id);

        assertEquals(expectedDto, actualDto);
        verify(userCache).put(id, dbUser);
    }

    @Test
    void getUserById_shouldThrowException_whenUserNotFound() {
        Long id = 3L;

        when(userCache.get(id)).thenReturn(null);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserById(id)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void createUser_shouldSaveUserAndReturnResponse() {
        UserRequestDto requestDto = new UserRequestDto("John Doe", "john@example.com", "+123456789");
        User userToSave = new User();
        userToSave.setName("John Doe");
        userToSave.setEmail("john@example.com");
        userToSave.setPhone("+123456789");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setPhone("+123456789");

        UserResponseDto responseDto = new UserResponseDto(1L, "John Doe", "john@example.com", "+123456789");

        when(userMapper.toUser(requestDto)).thenReturn(userToSave);
        when(userRepository.save(userToSave)).thenReturn(savedUser);
        when(userMapper.toUserResponseDto(savedUser)).thenReturn(responseDto);

        UserResponseDto result = userService.createUser(requestDto);

        assertEquals(responseDto, result);
        verify(userMapper).toUser(requestDto);
        verify(userRepository).save(userToSave);
        verify(userCache).put(savedUser.getId(), savedUser);
        verify(userMapper).toUserResponseDto(savedUser);
    }

    @Test
    void updateUser_shouldUpdateAndReturnUpdatedUser_whenUserExists() {
        Long id = 1L;
        UserRequestDto requestDto = new UserRequestDto("New Name", "new@example.com", "123456789");

        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");
        existingUser.setPhone("987654321");

        User updatedUser = new User();
        updatedUser.setId(id);
        updatedUser.setName("New Name");
        updatedUser.setEmail("new@example.com");
        updatedUser.setPhone("123456789");

        UserResponseDto responseDto = new UserResponseDto(id, "New Name", "new@example.com", "123456789");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.toUserResponseDto(updatedUser)).thenReturn(responseDto);

        UserResponseDto result = userService.updateUser(id, requestDto);

        assertEquals(responseDto, result);

        assertEquals("New Name", existingUser.getName());
        assertEquals("new@example.com", existingUser.getEmail());
        assertEquals("123456789", existingUser.getPhone());

        verify(userCache).put(id, updatedUser);
    }

    @Test
    void updateUser_shouldThrowException_whenUserNotFound() {
        Long id = 2L;
        UserRequestDto requestDto = new UserRequestDto("Name", "email@example.com", "123456789");

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUser(id, requestDto)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository, never()).save(any());
        verify(userCache, never()).put(any(), any());
        verify(userMapper, never()).toUserResponseDto(any());
    }

    @Test
    void deleteUserById_shouldRemoveUserAndEvictFromCache_whenUserExists() {
        Long id = 1L;

        User user = new User();
        user.setId(id);

        Ride ride1 = new Ride();
        Ride ride2 = new Ride();

        List<Ride> rides = new ArrayList<>();
        rides.add(ride1);
        rides.add(ride2);

        ride1.setPassengers(new ArrayList<>(List.of(user)));
        ride2.setPassengers(new ArrayList<>(List.of(user)));

        user.setRidesAsPassenger(rides);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.deleteUserById(id);

        verify(userRepository).deleteById(id);
        verify(userCache).remove(id);

        assertFalse(ride1.getPassengers().contains(user));
        assertFalse(ride2.getPassengers().contains(user));
    }

    @Test
    void deleteUserById_shouldThrowException_whenUserNotFound() {
        Long id = 2L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.deleteUserById(id)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository, never()).deleteById(any());
        verify(userCache, never()).remove(any());
    }
}
