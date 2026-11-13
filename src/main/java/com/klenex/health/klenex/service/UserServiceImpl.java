package com.klenex.health.klenex.service;

import com.klenex.health.klenex.dto.UserDto;
import com.klenex.health.klenex.model.Doctor;
import com.klenex.health.klenex.model.Role;
import com.klenex.health.klenex.model.User;
import com.klenex.health.klenex.repository.DoctorRepository;
import com.klenex.health.klenex.repository.RoleRepository;
import com.klenex.health.klenex.repository.UserRepository;
import com.klenex.health.klenex.util.TbConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    /** The roles an admin may hand out. */
    public static final List<String> ASSIGNABLE_ROLES =
            List.of("ROLE_ADMIN", "ROLE_STAFF", "ROLE_DOCTOR", TbConstants.Roles.USER);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void saveUser(UserDto userDto) {
        Role role = roleRepository.findByName(TbConstants.Roles.USER);

        if (role == null)
            role = roleRepository.save(new Role(TbConstants.Roles.USER));

        User user = new User(userDto.getName(), userDto.getEmail(), passwordEncoder.encode(userDto.getPassword()),
                Arrays.asList(role));
        userRepository.save(user);
    }

    @Override
    public User findUserByEmail(String email){ return userRepository.findByEmail(email); }

    @Override
    @Transactional(readOnly = true)
    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void assignRole(Long userId, String roleName, String actingEmail) {
        if (roleName == null || !ASSIGNABLE_ROLES.contains(roleName)) {
            throw new IllegalArgumentException("Unknown role.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("That user no longer exists."));

        // An admin who could demote themselves could leave the system with no admin at all.
        if (user.getEmail().equalsIgnoreCase(actingEmail)) {
            throw new IllegalArgumentException("You cannot change your own role.");
        }

        if ("ROLE_DOCTOR".equals(roleName)) {
            linkDoctorRecord(user);
        }

        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            role = roleRepository.save(new Role(roleName));
        }
        user.setRoles(new ArrayList<>(List.of(role)));
        userRepository.save(user);
    }

    /**
     * A doctor account needs a doctor record (that is what the doctor dashboard shows). Link the record that
     * has the same email, and refuse the change if there is none, so nobody ends up with the doctor role and
     * nothing behind it.
     */
    private void linkDoctorRecord(User user) {
        Optional<Doctor> linked = doctorRepository.findByUserEmail(user.getEmail());
        if (linked.isPresent()) {
            return;
        }
        List<Doctor> sameEmail = doctorRepository.findAllByEmailIgnoreCase(user.getEmail());
        if (sameEmail.isEmpty()) {
            throw new IllegalArgumentException("There is no doctor record with the email " + user.getEmail()
                    + ". Add the doctor under Doctors first, using the same email.");
        }
        Doctor doctor = sameEmail.stream().filter(d -> d.getUser() == null).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "That doctor record already belongs to another account."));
        doctor.setUser(user);
        doctorRepository.save(doctor);
    }
}
