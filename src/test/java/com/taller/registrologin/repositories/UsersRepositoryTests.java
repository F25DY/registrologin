package com.taller.registrologin.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.taller.registrologin.models.Users;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsersRepositoryTests {

    @Autowired
    private UsersRepository usersRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        Users user = new Users();
        user.setEmail("test.user@registrologin.local");
        user.setPassword("encoded-test-password");
        user.setFirstName("Prueba");
        user.setLastName("Registro");

        Users savedUser = usersRepository.saveAndFlush(user);

        assertThat(savedUser.getId()).isNotNull();
        var foundUser = usersRepository.findByEmail("test.user@registrologin.local");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.orElseThrow().getId()).isEqualTo(savedUser.getId());
    }
}
