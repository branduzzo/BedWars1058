package it.branduzzo.nextbedwars.api.util;

public final class Theme {

    private static String primary = "#00C853";
    private static String secondary = "#FFFFFF";
    private static String accent = "#D32F2F";
    private static String prefix = "";

    private Theme() {
    }

    public static void load(String primary, String secondary, String accent, String prefix) {
        Theme.primary = valid(primary, "#00C853");
        Theme.secondary = valid(secondary, "#FFFFFF");
        Theme.accent = valid(accent, "#D32F2F");
        Theme.prefix = prefix == null ? "" : prefix;
    }

    private static String valid(String value, String fallback) {
        if (value == null || !value.matches("#[0-9a-fA-F]{6}")) {
            return fallback;
        }
        return value;
    }

    public static String getPrimary() {
        return primary;
    }

    public static String getSecondary() {
        return secondary;
    }

    public static String getAccent() {
        return accent;
    }

    public static String getPrefix() {
        return prefix;
    }

    public static String hex(String hex) {
        String value = valid(hex, "#FFFFFF").substring(1);
        StringBuilder builder = new StringBuilder("\u00a7x");
        for (char c : value.toCharArray()) {
            builder.append('\u00a7').append(c);
        }
        return builder.toString();
    }

    public static String plainHex(String hex) {
        return hex(hex);
    }

    public static String gradient(String text, String from, String to) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        int[] start = rgb(valid(from, primary));
        int[] end = rgb(valid(to, secondary));
        StringBuilder builder = new StringBuilder();
        int visible = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&' && i + 1 < text.length()) {
                builder.append(c).append(text.charAt(++i));
                continue;
            }
            double ratio = text.length() == 1 ? 0 : (double) visible / (text.length() - 1);
            int r = (int) Math.round(start[0] + (end[0] - start[0]) * ratio);
            int g = (int) Math.round(start[1] + (end[1] - start[1]) * ratio);
            int b = (int) Math.round(start[2] + (end[2] - start[2]) * ratio);
            builder.append(hex(String.format("#%02X%02X%02X", r, g, b))).append(c);
            visible++;
        }
        return builder.toString();
    }

    private static int[] rgb(String hex) {
        String value = hex.substring(1);
        return new int[]{Integer.parseInt(value.substring(0, 2), 16),
                Integer.parseInt(value.substring(2, 4), 16),
                Integer.parseInt(value.substring(4, 6), 16)};
    }

    public static String apply(String message) {
        if (message == null) {
            return null;
        }
        return legacyToHex(inlineHex(message
                .replace("{primary}", hex(primary))
                .replace("{secondary}", hex(secondary))
                .replace("{accent}", hex(accent))
                .replace("{themePrefix}", prefix)));
    }

    private static String inlineHex(String text) {
        if (text == null || text.indexOf('#') < 0) {
            return text;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("#([0-9a-fA-F]{6})").matcher(text);
        StringBuilder builder = new StringBuilder(text.length() + 32);
        while (matcher.find()) {
            matcher.appendReplacement(builder, java.util.regex.Matcher.quoteReplacement(hex(matcher.group())));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private static String legacyToHex(String text) {
        if (text == null || text.indexOf('&') < 0) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length() + 64);
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '&' && i + 1 < text.length()) {
                String replacement = legacyHex(Character.toLowerCase(text.charAt(i + 1)));
                if (replacement != null) {
                    builder.append(replacement);
                    i++;
                    continue;
                }
            }
            builder.append(current);
        }
        return builder.toString();
    }

    private static String legacyHex(char code) {
        return switch (code) {
            case '0' -> hex("#000000");
            case '1' -> hex("#0D47A1");
            case '2' -> hex("#00796B");
            case '3' -> hex("#00838F");
            case '4' -> hex("#B71C1C");
            case '5' -> hex("#6A1B9A");
            case '6' -> hex("#EF6C00");
            case '7' -> hex("#9E9E9E");
            case '8' -> hex("#616161");
            case '9' -> hex("#1E88E5");
            case 'a' -> hex(primary);
            case 'b' -> hex("#4DD0E1");
            case 'c' -> hex(accent);
            case 'd' -> hex("#F06292");
            case 'e' -> hex("#FFD54F");
            case 'f' -> hex(secondary);
            default -> null;
        };
    }
}
