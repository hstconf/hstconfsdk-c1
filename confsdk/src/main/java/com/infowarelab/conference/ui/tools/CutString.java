package com.infowarelab.conference.ui.tools;


public class CutString {
    public static String cutString(String s, int num) {
        try {
            s = idgui(s, num);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            if (s.length() > num) {
                s = s.substring(0, num) + "…";
            }
        }
        return s;
    }

    private static String idgui(String s, int num) throws Exception {
        int changdu = s.getBytes("GBK").length;
        if (changdu > num) {
            s = s.substring(0, s.length() - 1);
            s = idgui2(s, num) + "…";
        }
        return s;
    }

    private static String idgui2(String s, int num) throws Exception {
        int changdu = s.getBytes("GBK").length;
        if (changdu > num) {
            s = s.substring(0, s.length() - 1);
            s = idgui2(s, num);
        }
        return s;
    }
}
