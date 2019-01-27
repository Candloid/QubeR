package com.quber.utility;

public class Obfuscator {

    public static String obfuscateString(String message, String password, boolean decode) {
        try {
            if (decode)
                message = new String(android.util.Base64.decode(message, android.util.Base64.DEFAULT));
            //message = new String(Base64.getDecoder().decode(message));
            int ml = message.length();
            int pl = password.length();
            String key = password;
            for (int i = 0; i < (int) (Math.ceil((double) ml / (double) pl)) - 1; i++)
                key += password;
            key = key.substring(0, message.length());
            char[] result = message.toCharArray();
            char[] keys = key.toCharArray();

            for (int i = 0; i <= message.length() - 1; i++) {
                result[i] = (char) (result[i] ^ keys[i]);
            }
            if (decode)
                return new String(result);
            else
                return android.util.Base64.encodeToString(new String(result)
                        .getBytes(), android.util.Base64.DEFAULT);
        }catch (Exception e){
            return "Invalid data or short key!";
        }
    }
}
