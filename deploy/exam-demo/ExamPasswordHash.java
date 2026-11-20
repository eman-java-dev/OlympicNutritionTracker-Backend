import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

class ExamPasswordHash {
    public static void main(String[] args) throws ReflectiveOperationException, java.io.IOException {
        var input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String password = input.readLine();
        if (password == null || password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Use at least 12 characters and at most 72 UTF-8 bytes.");
        }

        // Load the existing backend dependency in Java source-file mode.
        Class<?> bcrypt = Class.forName("org.springframework.security.crypto.bcrypt.BCrypt");
        String salt = (String) bcrypt.getMethod("gensalt").invoke(null);
        String hash = (String) bcrypt.getMethod("hashpw", String.class, String.class).invoke(null, password, salt);
        System.out.print(hash);
    }
}
