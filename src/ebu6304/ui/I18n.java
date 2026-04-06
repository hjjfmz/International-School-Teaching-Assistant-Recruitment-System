package ebu6304.ui;

import java.util.HashMap;
import java.util.Map;

public final class I18n {
    public enum Lang {
        ZH("中文"),
        EN("English"),
        ES("Español"),
        JA("日本語"),
        DE("Deutsch"),
        FR("Français"),
        PT("Português");

        private final String displayName;

        Lang(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static Lang lang = Lang.EN;

    private static final Map<String, String> zh = new HashMap<String, String>();
    private static final Map<String, String> en = new HashMap<String, String>();
    private static final Map<String, String> es = new HashMap<String, String>();
    private static final Map<String, String> ja = new HashMap<String, String>();
    private static final Map<String, String> de = new HashMap<String, String>();
    private static final Map<String, String> fr = new HashMap<String, String>();
    private static final Map<String, String> pt = new HashMap<String, String>();

    static {
        zh.put("app.title", "北邮国际学院TA招聘系统");
        en.put("app.title", "BUPT International School TA Recruitment System");
        es.put("app.title", "Sistema de Contratación de Ayudantes de Enseñanza de la Escuela Internacional de BUPT");
        ja.put("app.title", "BUPT国際学院TA募集システム");
        de.put("app.title", "BUPT International School TA-Bewerbungssystem");
        fr.put("app.title", "Système de Recrutement de TA de l'École Internationale de BUPT");
        pt.put("app.title", "Sistema de Recrutamento de TA da Escola Internacional da BUPT");

        zh.put("start.subtitle", "");
        en.put("start.subtitle", "");
        es.put("start.subtitle", "");
        ja.put("start.subtitle", "");
        de.put("start.subtitle", "");
        fr.put("start.subtitle", "");
        pt.put("start.subtitle", "");

        zh.put("start.button", "开始");
        en.put("start.button", "Start");
        es.put("start.button", "Iniciar");
        ja.put("start.button", "スタート");
        de.put("start.button", "Start");
        fr.put("start.button", "Démarrer");
        pt.put("start.button", "Iniciar");

        zh.put("login.tab.login", "登录");
        en.put("login.tab.login", "Login");
        es.put("login.tab.login", "Iniciar Sesión");
        ja.put("login.tab.login", "ログイン");
        de.put("login.tab.login", "Anmelden");
        fr.put("login.tab.login", "Connexion");
        pt.put("login.tab.login", "Login");
        zh.put("login.tab.register", "TA注册");
        en.put("login.tab.register", "TA Register");
        es.put("login.tab.register", "Registro de TA");
        ja.put("login.tab.register", "TA登録");
        de.put("login.tab.register", "TA-Registrierung");
        fr.put("login.tab.register", "Inscription TA");
        pt.put("login.tab.register", "Registro de TA");

        zh.put("role.ta", "TA申请人");
        en.put("role.ta", "TA Applicant");
        es.put("role.ta", "Postulante a TA");
        ja.put("role.ta", "TA応募者");
        de.put("role.ta", "TA-Bewerber");
        fr.put("role.ta", "Candidat TA");
        pt.put("role.ta", "Candidato TA");
        zh.put("role.mo", "模块组织者(MO)");
        en.put("role.mo", "Module Organiser (MO)");
        es.put("role.mo", "Organizador de Módulo (MO)");
        ja.put("role.mo", "モジュールオーガナイザー(MO)");
        de.put("role.mo", "Modulorganisator (MO)");
        fr.put("role.mo", "Organisateur de Module (MO)");
        pt.put("role.mo", "Organizador de Módulo (MO)");
        zh.put("role.admin", "管理员");
        en.put("role.admin", "Admin");
        es.put("role.admin", "Administrador");
        ja.put("role.admin", "管理者");
        de.put("role.admin", "Admin");
        fr.put("role.admin", "Administrateur");
        pt.put("role.admin", "Administrador");

        zh.put("common.logout", "退出登录");
        en.put("common.logout", "Logout");
        es.put("common.logout", "Cerrar Sesión");
        ja.put("common.logout", "ログアウト");
        de.put("common.logout", "Abmelden");
        fr.put("common.logout", "Déconnexion");
        pt.put("common.logout", "Sair");

        zh.put("login.account", "账号 (学生/教职工ID)");
        en.put("login.account", "Account (Student/Staff ID)");
        es.put("login.account", "Cuenta (ID de Estudiante/Profesor)");
        ja.put("login.account", "アカウント (学生/教職員ID)");
        de.put("login.account", "Konto (Student/Mitarbeiter-ID)");
        fr.put("login.account", "Compte (ID Étudiant/Enseignant)");
        pt.put("login.account", "Conta (ID do Estudante/Professor)");
        zh.put("login.password", "密码");
        en.put("login.password", "Password");
        es.put("login.password", "Contraseña");
        ja.put("login.password", "パスワード");
        de.put("login.password", "Passwort");
        fr.put("login.password", "Mot de passe");
        pt.put("login.password", "Senha");
        zh.put("login.button", "登录");
        en.put("login.button", "Login");
        es.put("login.button", "Iniciar Sesión");
        ja.put("login.button", "ログイン");
        de.put("login.button", "Anmelden");
        fr.put("login.button", "Connexion");
        pt.put("login.button", "Login");
        zh.put("login.forgot", "忘记密码");
        en.put("login.forgot", "Forgot password");
        es.put("login.forgot", "Olvidé mi contraseña");
        ja.put("login.forgot", "パスワードを忘れた");
        de.put("login.forgot", "Passwort vergessen");
        fr.put("login.forgot", "Mot de passe oublié");
        pt.put("login.forgot", "Esqueci a senha");

        zh.put("register.account", "学生ID (账号)*");
        en.put("register.account", "Student ID (Account)*");
        es.put("register.account", "ID de Estudiante (Cuenta)*");
        ja.put("register.account", "学生ID (アカウント)*");
        de.put("register.account", "Student-ID (Konto)*");
        fr.put("register.account", "ID Étudiant (Compte)*");
        pt.put("register.account", "ID do Estudante (Conta)*");
        zh.put("register.name", "姓名*");
        en.put("register.name", "Name*");
        es.put("register.name", "Nombre*");
        ja.put("register.name", "名前*");
        de.put("register.name", "Name*");
        fr.put("register.name", "Nom*");
        pt.put("register.name", "Nome*");
        zh.put("register.email", "邮箱*");
        en.put("register.email", "Email*");
        es.put("register.email", "Correo Electrónico*");
        ja.put("register.email", "メール*");
        de.put("register.email", "E-Mail*");
        fr.put("register.email", "Email*");
        pt.put("register.email", "Email*");
        zh.put("register.password", "密码*");
        en.put("register.password", "Password*");
        es.put("register.password", "Contraseña*");
        ja.put("register.password", "パスワード*");
        de.put("register.password", "Passwort*");
        fr.put("register.password", "Mot de passe*");
        pt.put("register.password", "Senha*");
        zh.put("register.password2", "确认密码*");
        en.put("register.password2", "Confirm Password*");
        es.put("register.password2", "Confirmar Contraseña*");
        ja.put("register.password2", "パスワードを确认*");
        de.put("register.password2", "Passwort bestätigen*");
        fr.put("register.password2", "Confirmer le mot de passe*");
        pt.put("register.password2", "Confirmar Senha*");
        zh.put("register.skills", "技能 (可选)");
        en.put("register.skills", "Skills (optional)");
        es.put("register.skills", "Habilidades (opcional)");
        ja.put("register.skills", "スキル (オプション)");
        de.put("register.skills", "Fähigkeiten (optional)");
        fr.put("register.skills", "Compétences (optionnel)");
        pt.put("register.skills", "Habilidades (opcional)");
        zh.put("register.cv", "简历路径 (PDF/Word)*");
        en.put("register.cv", "CV Path (PDF/Word)*");
        es.put("register.cv", "Ruta de CV (PDF/Word)*");
        ja.put("register.cv", "CVパス (PDF/Word)*");
        de.put("register.cv", "Lebenslauf-Pfad (PDF/Word)*");
        fr.put("register.cv", "Chemin du CV (PDF/Word)*");
        pt.put("register.cv", "Caminho do CV (PDF/Word)*");
        zh.put("register.browse", "浏览");
        en.put("register.browse", "Browse");
        es.put("register.browse", "Navegar");
        ja.put("register.browse", "参照");
        de.put("register.browse", "Durchsuchen");
        fr.put("register.browse", "Parcourir");
        pt.put("register.browse", "Procurar");
        zh.put("register.agree", "我同意注册条款*");
        en.put("register.agree", "I agree to the registration terms*");
        es.put("register.agree", "Acepto los términos de registro*");
        ja.put("register.agree", "登録条件に同意する*");
        de.put("register.agree", "Ich stimme den Registrierungsbedingungen zu*");
        fr.put("register.agree", "J'accepte les conditions d'inscription*");
        pt.put("register.agree", "Concordo com os termos de registro*");
        zh.put("register.button", "注册");
        en.put("register.button", "Register");
        es.put("register.button", "Registrarse");
        ja.put("register.button", "登録");
        de.put("register.button", "Registrieren");
        fr.put("register.button", "S'inscrire");
        pt.put("register.button", "Registrar");

        zh.put("forgot.title", "密码重置");
        en.put("forgot.title", "Password Reset");
        es.put("forgot.title", "Restablecimiento de Contraseña");
        ja.put("forgot.title", "パスワード再設定");
        de.put("forgot.title", "Passwort zurücksetzen");
        fr.put("forgot.title", "Réinitialiser le mot de passe");
        pt.put("forgot.title", "Redefinir Senha");
        zh.put("forgot.verify", "验证邮箱 (仅TA)");
        en.put("forgot.verify", "Verify Email (TA only)");
        es.put("forgot.verify", "Verificar Correo (solo TA)");
        ja.put("forgot.verify", "メール確認 (TAのみ)");
        de.put("forgot.verify", "E-Mail verifizieren (nur TA)");
        fr.put("forgot.verify", "Vérifier l'email (seulement TA)");
        pt.put("forgot.verify", "Verificar Email (apenas TA)");
        zh.put("forgot.newpass", "新密码");
        en.put("forgot.newpass", "New Password");
        es.put("forgot.newpass", "Nueva Contraseña");
        ja.put("forgot.newpass", "新しいパスワード");
        de.put("forgot.newpass", "Neues Passwort");
        fr.put("forgot.newpass", "Nouveau mot de passe");
        pt.put("forgot.newpass", "Nova Senha");
        zh.put("forgot.newpass2", "确认新密码");
        en.put("forgot.newpass2", "Confirm New Password");
        es.put("forgot.newpass2", "Confirmar Nueva Contraseña");
        ja.put("forgot.newpass2", "新しいパスワードを確認");
        de.put("forgot.newpass2", "Neues Passwort bestätigen");
        fr.put("forgot.newpass2", "Confirmer le nouveau mot de passe");
        pt.put("forgot.newpass2", "Confirmar Nova Senha");
        zh.put("forgot.button", "重置");
        en.put("forgot.button", "Reset");
        es.put("forgot.button", "Restablecer");
        ja.put("forgot.button", "リセット");
        de.put("forgot.button", "Zurücksetzen");
        fr.put("forgot.button", "Réinitialiser");
        pt.put("forgot.button", "Redefinir");
        zh.put("common.back", "返回");
        en.put("common.back", "Back");
        es.put("common.back", "Volver");
        ja.put("common.back", "戻る");
        de.put("common.back", "Zurück");
        fr.put("common.back", "Retour");
        pt.put("common.back", "Voltar");

        zh.put("status.ready", "本地文件已同步");
        en.put("status.ready", "Local files are synced");
        es.put("status.ready", "Archivos locales sincronizados");
        ja.put("status.ready", "ローカルファイルが同期されています");
        de.put("status.ready", "Lokale Dateien sind synchronisiert");
        fr.put("status.ready", "Fichiers locaux synchronisés");
        pt.put("status.ready", "Arquivos locais sincronizados");
        zh.put("status.processing", "处理数据中");
        en.put("status.processing", "Processing data");
        es.put("status.processing", "Procesando datos");
        ja.put("status.processing", "データ処理中");
        de.put("status.processing", "Daten werden verarbeitet");
        fr.put("status.processing", "Traitement des données");
        pt.put("status.processing", "Processando dados");
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
        } else if (lang == Lang.ES) {
            String v = es.get(key);
            return v == null ? key : v;
        } else if (lang == Lang.JA) {
            String v = ja.get(key);
            return v == null ? key : v;
        } else if (lang == Lang.DE) {
            String v = de.get(key);
            return v == null ? key : v;
        } else if (lang == Lang.FR) {
            String v = fr.get(key);
            return v == null ? key : v;
        } else if (lang == Lang.PT) {
            String v = pt.get(key);
            return v == null ? key : v;
        }
        String v = zh.get(key);
        return v == null ? key : v;
    }
}
