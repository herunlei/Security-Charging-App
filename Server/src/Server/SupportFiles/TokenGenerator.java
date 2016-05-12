package Server.SupportFiles;

import java.security.SecureRandom;

/**
 * Verify token generator
 */
public class TokenGenerator {
    private static SecureRandom random = new SecureRandom();

    /**
     * @param username
     * @return
     */
    public synchronized String generateToken(String username) {
        long longToken = Math.abs(random.nextLong());
        String random = Long.toString(longToken, 16);
        return (username + ":" + random);
    }
}
