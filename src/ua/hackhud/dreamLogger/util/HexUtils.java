package ua.hackhud.dreamLogger.util;

public class HexUtils {

    public static String stripHexColors(String input) {
        return input.replaceAll("#[A-Fa-f0-9]{6}", "");
    }
}
