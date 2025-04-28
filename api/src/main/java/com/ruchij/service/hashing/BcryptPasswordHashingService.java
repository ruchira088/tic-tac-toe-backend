package com.ruchij.service.hashing;

import org.mindrot.jbcrypt.BCrypt;

public class BcryptPasswordHashingService implements PasswordHashingService {
    @Override
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}
