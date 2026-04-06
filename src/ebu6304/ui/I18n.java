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
        zh.put("app.title", "北邮国际学院TA招聘系统");
        en.put("app.title", "BUPT International School TA Recruitment System");

        zh.put("start.subtitle", "");
        en.put("start.subtitle", "");

        zh.put("start.button", "开始");
        en.put("start.button", "Start");

        zh.put("login.tab.login", "登录");
        en.put("login.tab.login", "Login");
        zh.put("login.tab.register", "TA注册");
        en.put("login.tab.register", "TA Register");

        zh.put("role.ta", "TA申请人");
        en.put("role.ta", "TA Applicant");
        zh.put("role.mo", "模块组织者(MO)");
        en.put("role.mo", "Module Organiser (MO)");
        zh.put("role.admin", "管理员");
        en.put("role.admin", "Admin");

        zh.put("common.logout", "退出登录");
        en.put("common.logout", "Logout");

        zh.put("login.account", "账号 (学生/教职工ID)");
        en.put("login.account", "Account (Student/Staff ID)");
        zh.put("login.password", "密码");
        en.put("login.password", "Password");
        zh.put("login.button", "登录");
        en.put("login.button", "Login");
        zh.put("login.forgot", "忘记密码");
        en.put("login.forgot", "Forgot password");

        zh.put("register.account", "学生ID (账号)*");
        en.put("register.account", "Student ID (Account)*");
        zh.put("register.name", "姓名*");
        en.put("register.name", "Name*");
        zh.put("register.email", "邮箱*");
        en.put("register.email", "Email*");
        zh.put("register.password", "密码*");
        en.put("register.password", "Password*");
        zh.put("register.password2", "确认密码*");
        en.put("register.password2", "Confirm Password*");
        zh.put("register.skills", "技能 (可选)");
        en.put("register.skills", "Skills (optional)");
        zh.put("register.cv", "简历路径 (PDF/Word)*");
        en.put("register.cv", "CV Path (PDF/Word)*");
        zh.put("register.browse", "浏览");
        en.put("register.browse", "Browse");
        zh.put("register.agree", "我同意注册条款*");
        en.put("register.agree", "I agree to the registration terms*");
        zh.put("register.button", "注册");
        en.put("register.button", "Register");

        zh.put("forgot.title", "密码重置");
        en.put("forgot.title", "Password Reset");
        zh.put("forgot.verify", "验证邮箱 (仅TA)");
        en.put("forgot.verify", "Verify Email (TA only)");
        zh.put("forgot.newpass", "新密码");
        en.put("forgot.newpass", "New Password");
        zh.put("forgot.newpass2", "确认新密码");
        en.put("forgot.newpass2", "Confirm New Password");
        zh.put("forgot.button", "重置");
        en.put("forgot.button", "Reset");
        zh.put("common.back", "返回");
        en.put("common.back", "Back");

        zh.put("status.ready", "本地文件已同步");
        en.put("status.ready", "Local files are synced");
        zh.put("status.processing", "处理数据中");
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
