package com.klenex.health.klenex.service;

import com.klenex.health.klenex.dto.UserDto;
import com.klenex.health.klenex.model.User;

public interface UserService {
    void saveUser(UserDto userDto);
    User findUserByEmail(String email);
}
