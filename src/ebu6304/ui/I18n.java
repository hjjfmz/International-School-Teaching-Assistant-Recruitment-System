package ebu6304.ui;

import java.util.HashMap;
import java.util.Map;

public final class I18n {
    public enum Lang {
        ZH,
        EN
    }

    private static Lang lang = Lang.EN;

    private static final Map<String, String> zh = new HashMap<String, String>();
    private static final Map<String, String> en = new HashMap<String, String>();

    static {
        zh.put("app.title", "BUPT International School TA Recruitment System");
        en.put("app.title", "BUPT International School TA Recruitment System");

        zh.put("start.subtitle", "All data is stored locally under the data folder");
        en.put("start.subtitle", "All data is stored locally under the data folder");

        zh.put("start.button", "Start");
        en.put("start.button", "Start");

        zh.put("login.tab.login", "Login");
        en.put("login.tab.login", "Login");
        zh.put("login.tab.register", "TA Register");
        en.put("login.tab.register", "TA Register");

        zh.put("role.ta", "TA Applicant");
        en.put("role.ta", "TA Applicant");
        zh.put("role.mo", "Module Organiser (MO)");
        en.put("role.mo", "Module Organiser (MO)");
        zh.put("role.admin", "Admin");
        en.put("role.admin", "Admin");

        zh.put("common.logout", "Logout");
        en.put("common.logout", "Logout");

        zh.put("login.account", "Account (Student/Staff ID)");
        en.put("login.account", "Account (Student/Staff ID)");
        zh.put("login.password", "Password");
        en.put("login.password", "Password");
        zh.put("login.button", "Login");
        en.put("login.button", "Login");
        zh.put("login.forgot", "Forgot password");
        en.put("login.forgot", "Forgot password");

        zh.put("register.account", "Student ID (Account)*");
        en.put("register.account", "Student ID (Account)*");
        zh.put("register.name", "Name*");
        en.put("register.name", "Name*");
        zh.put("register.email", "Email*");
        en.put("register.email", "Email*");
        zh.put("register.password", "Password*");
        en.put("register.password", "Password*");
        zh.put("register.password2", "Confirm Password*");
        en.put("register.password2", "Confirm Password*");
        zh.put("register.skills", "Skills (optional)");
        en.put("register.skills", "Skills (optional)");
        zh.put("register.cv", "CV Path (PDF/Word)*");
        en.put("register.cv", "CV Path (PDF/Word)*");
        zh.put("register.browse", "Browse");
        en.put("register.browse", "Browse");
        zh.put("register.agree", "I agree to the registration terms*");
        en.put("register.agree", "I agree to the registration terms*");
        zh.put("register.button", "Register");
        en.put("register.button", "Register");

        zh.put("forgot.title", "Password Reset");
        en.put("forgot.title", "Password Reset");
        zh.put("forgot.verify", "Verify Email (TA only)");
        en.put("forgot.verify", "Verify Email (TA only)");
        zh.put("forgot.newpass", "New Password");
        en.put("forgot.newpass", "New Password");
        zh.put("forgot.newpass2", "Confirm New Password");
        en.put("forgot.newpass2", "Confirm New Password");
        zh.put("forgot.button", "Reset");
        en.put("forgot.button", "Reset");
        zh.put("common.back", "Back");
        en.put("common.back", "Back");

        zh.put("status.ready", "Local files are synced");
        en.put("status.ready", "Local files are synced");
        zh.put("status.processing", "Processing data");
        en.put("status.processing", "Processing data");
    }

    private I18n() {}

    public static Lang lang() {
        return lang;
    }

    public static void setLang(Lang l) {
        if (l != null) lang = l;
    }

    public static String t(String key) {
        if (key == null) key = "";
        if (lang == Lang.EN) {
            String v = en.get(key);
            return v == null ? key : v;
        }
        String v = zh.get(key);
        return v == null ? key : v;
    }
}
