package com.ruchij.service.hashing;

public interface PasswordHashingService {
    String hashPassword(String password);

    boolean verifyPassword(String password, String hashedPassword);
}
