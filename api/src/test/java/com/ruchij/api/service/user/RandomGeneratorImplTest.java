package com.ruchij.api.service.user;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;

class RandomGeneratorImplTest {
    @Test
    void testNames() {
        Faker instance = Faker.instance();
        for (int i = 0; i < 10; i++) {
            System.out.println(instance.color().name());
            System.out.println(instance.animal().name());
        }
    }

}