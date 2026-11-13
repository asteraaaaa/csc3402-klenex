package com.klenex.health.klenex.service;

import com.klenex.health.klenex.dto.UserDto;
import com.klenex.health.klenex.model.User;

import java.util.List;

public interface UserService {
    void saveUser(UserDto userDto);

    User findUserByEmail(String email);

    List<User> listAllUsers();

    /**
     * Gives a user exactly one role (replacing the old one). Throws {@link IllegalArgumentException}
     * with a message that is safe to show when the change is not allowed.
     *
     * @param actingEmail the admin making the change; admins cannot change their own role
     */
    void assignRole(Long userId, String roleName, String actingEmail);
}
