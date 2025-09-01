package it.polimi.tiw2025gioiamarra.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
//    public static String hash(String plain) {
//        return BCrypt.hashpw(plain, BCrypt.gensalt(12));
//    }

    public static boolean matches(String plain, String hashed) {
        return BCrypt.checkpw(plain, hashed);
    }
}
