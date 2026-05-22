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
        en.put("app.title", "BUPT International School TA Recruitment System");
        zh.put("app.title", "\u5317\u90AE\u56FD\u9645\u5B66\u9662 TA \u62DB\u8058\u7CFB\u7EDF");

        en.put("start.subtitle", "");
        zh.put("start.subtitle", "");

        en.put("start.button", "Start");
        zh.put("start.button", "\u5F00\u59CB");

        en.put("login.tab.login", "Login");
        zh.put("login.tab.login", "\u767B\u5F55");
        en.put("login.tab.register", "TA Register");
        zh.put("login.tab.register", "TA \u6CE8\u518C");

        en.put("role.ta", "TA Applicant");
        zh.put("role.ta", "TA \u7533\u8BF7\u4EBA");
        en.put("role.mo", "Module Organiser (MO)");
        zh.put("role.mo", "\u8BFE\u7A0B\u7EC4\u7EC7\u8005 (MO)");
        en.put("role.admin", "Admin");
        zh.put("role.admin", "\u7BA1\u7406\u5458");

        en.put("common.logout", "Logout");
        zh.put("common.logout", "\u9000\u51FA\u767B\u5F55");

        en.put("login.account", "Account (Student/Staff ID)");
        zh.put("login.account", "\u8D26\u53F7\uFF08\u5B66\u53F7/\u5DE5\u53F7\uFF09");
        en.put("login.password", "Password");
        zh.put("login.password", "\u5BC6\u7801");
        en.put("login.button", "Login");
        zh.put("login.button", "\u767B\u5F55");
        en.put("login.forgot", "Forgot password");
        zh.put("login.forgot", "\u5FD8\u8BB0\u5BC6\u7801");

        en.put("register.account", "Student ID (Account)*");
        zh.put("register.account", "\u5B66\u53F7\uFF08\u8D26\u53F7\uFF09*");
        en.put("register.name", "Name*");
        zh.put("register.name", "\u59D3\u540D*");
        en.put("register.email", "Email*");
        zh.put("register.email", "\u90AE\u7BB1*");
        en.put("register.password", "Password*");
        zh.put("register.password", "\u5BC6\u7801*");
        en.put("register.password2", "Confirm Password*");
        zh.put("register.password2", "\u786E\u8BA4\u5BC6\u7801*");
        en.put("register.skills", "Skills (optional)");
        zh.put("register.skills", "\u6280\u80FD\uFF08\u53EF\u9009\uFF09");
        en.put("register.cv", "CV Path (PDF/Word)*");
        zh.put("register.cv", "\u7B80\u5386\u8DEF\u5F84 (PDF/Word)*");
        en.put("register.browse", "Browse");
        zh.put("register.browse", "\u6D4F\u89C8");
        en.put("register.agree", "I agree to the registration terms*");
        zh.put("register.agree", "\u6211\u540C\u610F\u6CE8\u518C\u6761\u6B3E*");
        en.put("register.button", "Register");
        zh.put("register.button", "\u6CE8\u518C");

        en.put("forgot.title", "Password Reset");
        zh.put("forgot.title", "\u5BC6\u7801\u91CD\u7F6E");
        en.put("forgot.verify", "Verify Email (TA only)");
        zh.put("forgot.verify", "\u9A8C\u8BC1\u90AE\u7BB1\uFF08\u4EC5 TA\uFF09");
        en.put("forgot.newpass", "New Password");
        zh.put("forgot.newpass", "\u65B0\u5BC6\u7801");
        en.put("forgot.newpass2", "Confirm New Password");
        zh.put("forgot.newpass2", "\u786E\u8BA4\u65B0\u5BC6\u7801");
        en.put("forgot.button", "Reset");
        zh.put("forgot.button", "\u91CD\u7F6E");
        en.put("common.backtext", "Back");
        zh.put("common.backtext", "\u8FD4\u56DE");

        en.put("status.ready", "Local files are synced");
        zh.put("status.ready", "\u672C\u5730\u6587\u4EF6\u5DF2\u540C\u6B65");
        en.put("status.processing", "Processing data");
        zh.put("status.processing", "\u6570\u636E\u5904\u7406\u4E2D");

        // ── Navigation items ──
        en.put("nav.ta.home", "TA Home");
        zh.put("nav.ta.home", "TA \u9996\u9875");
        en.put("nav.ta.profile", "Profile");
        zh.put("nav.ta.profile", "\u4E2A\u4EBA\u8D44\u6599");
        en.put("nav.ta.resume", "Resume");
        zh.put("nav.ta.resume", "\u7B80\u5386");
        en.put("nav.ta.jobs", "Job Search");
        zh.put("nav.ta.jobs", "\u5C97\u4F4D\u641C\u7D22");
        en.put("nav.ta.myapps", "My Applications");
        zh.put("nav.ta.myapps", "\u6211\u7684\u7533\u8BF7");
        en.put("nav.ta.status", "Application Status");
        zh.put("nav.ta.status", "\u7533\u8BF7\u72B6\u6001");

        en.put("nav.mo.home", "MO Home");
        zh.put("nav.mo.home", "MO \u9996\u9875");
        en.put("nav.mo.post", "Post Job");
        zh.put("nav.mo.post", "\u53D1\u5E03\u5C97\u4F4D");
        en.put("nav.mo.applicants", "Applicants");
        zh.put("nav.mo.applicants", "\u7533\u8BF7\u8005");
        en.put("nav.mo.results", "Results");
        zh.put("nav.mo.results", "\u7ED3\u679C");
        en.put("nav.mo.myposts", "My Posts");
        zh.put("nav.mo.myposts", "\u6211\u7684\u53D1\u5E03");

        en.put("nav.admin.home", "Admin Home");
        zh.put("nav.admin.home", "\u7BA1\u7406\u9996\u9875");
        en.put("nav.admin.users", "User Management");
        zh.put("nav.admin.users", "\u7528\u6237\u7BA1\u7406");
        en.put("nav.admin.workload", "TA Workload");
        zh.put("nav.admin.workload", "TA \u5DE5\u4F5C\u91CF");
        en.put("nav.admin.jobdata", "Job Data");
        zh.put("nav.admin.jobdata", "\u5C97\u4F4D\u6570\u636E");
        en.put("nav.admin.config", "System Config");
        zh.put("nav.admin.config", "\u7CFB\u7EDF\u914D\u7F6E");
        en.put("nav.admin.export", "Data Export");
        zh.put("nav.admin.export", "\u6570\u636E\u5BFC\u51FA");
        en.put("nav.admin.logs", "Operation Logs");
        zh.put("nav.admin.logs", "\u64CD\u4F5C\u65E5\u5FD7");

        // ── Common buttons ──
        en.put("common.refresh", "Refresh");
        zh.put("common.refresh", "\u5237\u65B0");
        en.put("common.save", "Save");
        zh.put("common.save", "\u4FDD\u5B58");
        en.put("common.delete", "Delete");
        zh.put("common.delete", "\u5220\u9664");
        en.put("common.edit", "Edit");
        zh.put("common.edit", "\u7F16\u8F91");
        en.put("common.search", "Search");
        zh.put("common.search", "\u641C\u7D22");
        en.put("common.export", "Export");
        zh.put("common.export", "\u5BFC\u51FA");
        en.put("common.confirm", "Confirm");
        zh.put("common.confirm", "\u786E\u8BA4");
        en.put("common.cancel", "Cancel");
        zh.put("common.cancel", "\u53D6\u6D88");
        en.put("common.close", "Close");
        zh.put("common.close", "\u5173\u95ED");
        en.put("common.apply", "Apply");
        zh.put("common.apply", "\u7533\u8BF7");
        en.put("common.details", "Details");
        zh.put("common.details", "\u8BE6\u60C5");
        en.put("common.browse", "Browse");
        zh.put("common.browse", "\u6D4F\u89C8");
        en.put("common.submit", "Submit");
        zh.put("common.submit", "\u63D0\u4EA4");
        en.put("common.preview", "Preview");
        zh.put("common.preview", "\u9884\u89C8");
        en.put("common.accept", "Accept");
        zh.put("common.accept", "\u63A5\u53D7");
        en.put("common.reject", "Reject");
        zh.put("common.reject", "\u62D2\u7EDD");
        en.put("common.withdraw", "Withdraw");
        zh.put("common.withdraw", "\u64A4\u56DE");
        en.put("common.enable", "Enable");
        zh.put("common.enable", "\u542F\u7528");
        en.put("common.disable", "Disable");
        zh.put("common.disable", "\u7981\u7528");
        en.put("common.clear", "Clear");
        zh.put("common.clear", "\u6E05\u7A7A");
        en.put("common.reload", "Reload");
        zh.put("common.reload", "\u91CD\u65B0\u52A0\u8F7D");
        en.put("common.exportcsv", "Export CSV");
        zh.put("common.exportcsv", "\u5BFC\u51FA CSV");
        en.put("common.back", "\u003C Back");
        zh.put("common.back", "\u003C \u8FD4\u56DE");

        en.put("common.all", "All");
        zh.put("common.all", "\u5168\u90E8");

        // ── Common labels ──
        en.put("common.job", "Job:");
        zh.put("common.job", "\u5C97\u4F4D\uFF1A");
        en.put("common.status", "Status:");
        zh.put("common.status", "\u72B6\u6001\uFF1A");
        en.put("common.filter", "Filter:");
        zh.put("common.filter", "\u7B5B\u9009\uFF1A");
        en.put("common.search.label", "Search:");
        zh.put("common.search.label", "\u641C\u7D22\uFF1A");
        en.put("common.role", "Role");
        zh.put("common.role", "\u89D2\u8272");

        // ── Common messages ──
        en.put("msg.select.applicant", "Please select an applicant");
        zh.put("msg.select.applicant", "\u8BF7\u9009\u62E9\u4E00\u4E2A\u7533\u8BF7\u8005");
        en.put("msg.select.job", "Please select a job");
        zh.put("msg.select.job", "\u8BF7\u9009\u62E9\u4E00\u4E2A\u5C97\u4F4D");
        en.put("msg.select.user", "Please select a user");
        zh.put("msg.select.user", "\u8BF7\u9009\u62E9\u4E00\u4E2A\u7528\u6237");
        en.put("msg.select.application", "Please select an application");
        zh.put("msg.select.application", "\u8BF7\u9009\u62E9\u4E00\u4E2A\u7533\u8BF7");
        en.put("msg.select.profile", "Please select a profile row first.");
        zh.put("msg.select.profile", "\u8BF7\u5148\u9009\u62E9\u4E00\u884C\u8D44\u6599\u3002");
        en.put("msg.fields.required", "All fields marked with * are required");
        zh.put("msg.fields.required", "\u6240\u6709\u5E26 * \u7684\u5B57\u6BB5\u5747\u4E3A\u5FC5\u586B");
        en.put("msg.account.password.required", "Account and password are required");
        zh.put("msg.account.password.required", "\u8D26\u53F7\u548C\u5BC6\u7801\u4E3A\u5FC5\u586B\u9879");
        en.put("msg.password.mismatch", "Passwords do not match");
        zh.put("msg.password.mismatch", "\u5BC6\u7801\u4E0D\u4E00\u81F4");
        en.put("msg.email.required", "Email is required");
        zh.put("msg.email.required", "\u90AE\u7BB1\u4E3A\u5FC5\u586B\u9879");
        en.put("msg.operation.failed", "Operation failed");
        zh.put("msg.operation.failed", "\u64CD\u4F5C\u5931\u8D25");
        en.put("msg.export.success", "Exported");
        zh.put("msg.export.success", "\u5BFC\u51FA\u6210\u529F");
        en.put("msg.export.failed", "Export failed");
        zh.put("msg.export.failed", "\u5BFC\u51FA\u5931\u8D25");
        en.put("msg.save.failed", "Save failed");
        zh.put("msg.save.failed", "\u4FDD\u5B58\u5931\u8D25");
        en.put("msg.delete.failed", "Delete failed");
        zh.put("msg.delete.failed", "\u5220\u9664\u5931\u8D25");
        en.put("msg.withdraw.failed", "Withdraw failed");
        zh.put("msg.withdraw.failed", "\u64A4\u56DE\u5931\u8D25");
        en.put("msg.created", "Created");
        zh.put("msg.created", "\u521B\u5EFA\u6210\u529F");
        en.put("msg.deleted", "Deleted");
        zh.put("msg.deleted", "\u5DF2\u5220\u9664");
        en.put("msg.withdrawn", "Withdrawn");
        zh.put("msg.withdrawn", "\u5DF2\u64A4\u56DE");
        en.put("msg.upload.success", "Upload success");
        zh.put("msg.upload.success", "\u4E0A\u4F20\u6210\u529F");
        en.put("msg.register.success", "Registration successful. Please login.");
        zh.put("msg.register.success", "\u6CE8\u518C\u6210\u529F\uFF0C\u8BF7\u767B\u5F55\u3002");
        en.put("msg.login.failed", "Account or password incorrect");
        zh.put("msg.login.failed", "\u8D26\u53F7\u6216\u5BC6\u7801\u9519\u8BEF");
        en.put("msg.reset.success", "Password reset successful");
        zh.put("msg.reset.success", "\u5BC6\u7801\u91CD\u7F6E\u6210\u529F");
        en.put("msg.account.notfound", "Account not found");
        zh.put("msg.account.notfound", "\u8D26\u53F7\u672A\u627E\u5230");
        en.put("msg.verify.mismatch", "Verification information mismatch");
        zh.put("msg.verify.mismatch", "\u9A8C\u8BC1\u4FE1\u606F\u4E0D\u5339\u914D");
        en.put("msg.role.mismatch", "Account not found or role mismatch");
        zh.put("msg.role.mismatch", "\u8D26\u53F7\u672A\u627E\u5230\u6216\u89D2\u8272\u4E0D\u5339\u914D");
        en.put("msg.account.required", "Account and new password are required");
        zh.put("msg.account.required", "\u8D26\u53F7\u548C\u65B0\u5BC6\u7801\u4E3A\u5FC5\u586B");
        en.put("msg.status.updated", "Application {0}");
        zh.put("msg.status.updated", "\u7533\u8BF7\u5DF2{0}");
        en.put("msg.status.updated.batch", "{0} application(s) {1}");
        zh.put("msg.status.updated.batch", "{0} \u4E2A\u7533\u8BF7\u5DF2{1}");

        // ── TA Home ──
        en.put("ta.home.welcome", "Hi {0}, welcome to the TA recruitment system");
        zh.put("ta.home.welcome", "\u4F60\u597D {0}\uFF0C\u6B22\u8FCE\u4F7F\u7528 TA \u62DB\u8058\u7CFB\u7EDF");
        en.put("ta.home.browsejobs", "Browse latest jobs");
        zh.put("ta.home.browsejobs", "\u6D4F\u89C8\u6700\u65B0\u5C97\u4F4D");
        en.put("ta.home.mystatus", "My application status");
        zh.put("ta.home.mystatus", "\u6211\u7684\u7533\u8BF7\u72B6\u6001");
        en.put("ta.home.stats", "Stats");
        zh.put("ta.home.stats", "\u7EDF\u8BA1");
        en.put("ta.home.stats.text", "Total applied: {0}   Pending: {1}   Accepted: {2}   Rejected: {3}");
        zh.put("ta.home.stats.text", "\u603B\u7533\u8BF7: {0}   \u5F85\u5BA1: {1}   \u5DF2\u5F55\u53D6: {2}   \u5DF2\u62D2\u7EDD: {3}");

        // ── TA Profile ──
        en.put("ta.profile.title", "My Profile");
        zh.put("ta.profile.title", "\u6211\u7684\u8D44\u6599");
        en.put("ta.profile.hint", "What would you like to do?");
        zh.put("ta.profile.hint", "\u60A8\u60F3\u505A\u4EC0\u4E48\uFF1F");
        en.put("ta.profile.create", "Create a Profile");
        zh.put("ta.profile.create", "\u521B\u5EFA\u8D44\u6599");
        en.put("ta.profile.manage", "Manage My Profile");
        zh.put("ta.profile.manage", "\u7BA1\u7406\u6211\u7684\u8D44\u6599");
        en.put("ta.profile.noprofile", "No profile found. Please create a profile first.");
        zh.put("ta.profile.noprofile", "\u672A\u627E\u5230\u8D44\u6599\uFF0C\u8BF7\u5148\u521B\u5EFA\u8D44\u6599\u3002");
        en.put("ta.profile.noprofile.title", "No Profile");
        zh.put("ta.profile.noprofile.title", "\u65E0\u8D44\u6599");
        en.put("ta.profile.selecthint", "Select your profile to view or edit");
        zh.put("ta.profile.selecthint", "\u9009\u62E9\u60A8\u7684\u8D44\u6599\u4EE5\u67E5\u770B\u6216\u7F16\u8F91");
        en.put("ta.profile.create.title", "Create a Profile");
        zh.put("ta.profile.create.title", "\u521B\u5EFA\u8D44\u6599");
        en.put("ta.profile.edit.title", "Edit Profile");
        zh.put("ta.profile.edit.title", "\u7F16\u8F91\u8D44\u6599");
        en.put("ta.profile.delete", "Delete Profile");
        zh.put("ta.profile.delete", "\u5220\u9664\u8D44\u6599");
        en.put("ta.profile.save", "Save Profile");
        zh.put("ta.profile.save", "\u4FDD\u5B58\u8D44\u6599");
        en.put("ta.profile.savechanges", "Save Changes");
        zh.put("ta.profile.savechanges", "\u4FDD\u5B58\u66F4\u6539");
        en.put("ta.profile.created", "Profile created successfully.");
        zh.put("ta.profile.created", "\u8D44\u6599\u521B\u5EFA\u6210\u529F\u3002");
        en.put("ta.profile.updated", "Profile updated successfully.");
        zh.put("ta.profile.updated", "\u8D44\u6599\u66F4\u65B0\u6210\u529F\u3002");
        en.put("ta.profile.deleted", "Profile information deleted.");
        zh.put("ta.profile.deleted", "\u8D44\u6599\u4FE1\u606F\u5DF2\u5220\u9664\u3002");
        en.put("ta.profile.confirm.delete", "Are you sure you want to delete your profile information?\n(Email, skills, CV and description will be cleared.)");
        zh.put("ta.profile.confirm.delete", "\u786E\u5B9A\u8981\u5220\u9664\u60A8\u7684\u8D44\u6599\u4FE1\u606F\u5417\uFF1F\n\uFF08\u90AE\u7BB1\u3001\u6280\u80FD\u3001\u7B80\u5386\u548C\u63CF\u8FF0\u5C06\u88AB\u6E05\u7A7A\u3002\uFF09");
        en.put("ta.profile.confirm.delete.title", "Confirm Delete");
        zh.put("ta.profile.confirm.delete.title", "\u786E\u8BA4\u5220\u9664");
        en.put("ta.profile.label.account", "Account (Student ID)");
        zh.put("ta.profile.label.account", "\u8D26\u53F7\uFF08\u5B66\u53F7\uFF09");
        en.put("ta.profile.label.name", "Name");
        zh.put("ta.profile.label.name", "\u59D3\u540D");
        en.put("ta.profile.label.email", "Email *");
        zh.put("ta.profile.label.email", "\u90AE\u7BB1 *");
        en.put("ta.profile.label.skills", "Skills");
        zh.put("ta.profile.label.skills", "\u6280\u80FD");
        en.put("ta.profile.label.cv", "CV / Resume Path");
        zh.put("ta.profile.label.cv", "\u7B80\u5386\u8DEF\u5F84");
        en.put("ta.profile.label.desc", "Description");
        zh.put("ta.profile.label.desc", "\u63CF\u8FF0");
        en.put("ta.profile.unsupported.cv", "Unsupported CV format. Allowed: ");
        zh.put("ta.profile.unsupported.cv", "\u4E0D\u652F\u6301\u7684\u7B80\u5386\u683C\u5F0F\u3002\u5141\u8BB8: ");
        en.put("ta.profile.cv.savefailed", "Unable to save CV into project data folder");
        zh.put("ta.profile.cv.savefailed", "\u65E0\u6CD5\u5C06\u7B80\u5386\u4FDD\u5B58\u5230\u9879\u76EE\u6570\u636E\u6587\u4EF6\u5939");

        // ── TA Profile table headers ──
        en.put("ta.profile.col.account", "Account (ID)");
        zh.put("ta.profile.col.account", "\u8D26\u53F7 (ID)");
        en.put("ta.profile.col.name", "Name");
        zh.put("ta.profile.col.name", "\u59D3\u540D");
        en.put("ta.profile.col.email", "Email");
        zh.put("ta.profile.col.email", "\u90AE\u7BB1");
        en.put("ta.profile.col.skills", "Skills");
        zh.put("ta.profile.col.skills", "\u6280\u80FD");
        en.put("ta.profile.col.cv", "CV Path");
        zh.put("ta.profile.col.cv", "\u7B80\u5386\u8DEF\u5F84");

        // ── TA Resume ──
        en.put("ta.resume.title", "Resume");
        zh.put("ta.resume.title", "\u7B80\u5386");
        en.put("ta.resume.currentcv", "Current CV path");
        zh.put("ta.resume.currentcv", "\u5F53\u524D\u7B80\u5386\u8DEF\u5F84");
        en.put("ta.resume.reupload", "Re-upload");
        zh.put("ta.resume.reupload", "\u91CD\u65B0\u4E0A\u4F20");
        en.put("ta.resume.opencv", "Open CV");
        zh.put("ta.resume.opencv", "\u6253\u5F00\u7B80\u5386");
        en.put("ta.resume.openfailed", "Unable to open file");
        zh.put("ta.resume.openfailed", "\u65E0\u6CD5\u6253\u5F00\u6587\u4EF6");

        // ── TA Jobs ──
        en.put("ta.jobs.title", "Job Search");
        zh.put("ta.jobs.title", "\u5C97\u4F4D\u641C\u7D22");
        en.put("ta.jobs.hint", "Jobs are ranked by your current profile match and recommendation signals");
        zh.put("ta.jobs.hint", "\u5C97\u4F4D\u5DF2\u6309\u60A8\u5F53\u524D\u7684\u4E2A\u4EBA\u753B\u50CF\u5339\u914D\u5EA6\u548C\u63A8\u8350\u4FE1\u53F7\u6392\u5E8F");
        en.put("ta.jobs.already.applied", "You have already applied for this job");
        zh.put("ta.jobs.already.applied", "\u60A8\u5DF2\u7ECF\u7533\u8BF7\u4E86\u8FD9\u4E2A\u5C97\u4F4D");
        en.put("ta.jobs.confirm.apply", "Apply for the selected job?");
        zh.put("ta.jobs.confirm.apply", "\u7533\u8BF7\u9009\u4E2D\u7684\u5C97\u4F4D\uFF1F");
        en.put("ta.jobs.applied", "Application submitted");
        zh.put("ta.jobs.applied", "\u7533\u8BF7\u5DF2\u63D0\u4EA4");
        en.put("ta.jobs.col.id", "Job ID");
        zh.put("ta.jobs.col.id", "\u5C97\u4F4D ID");
        en.put("ta.jobs.col.title", "Title");
        zh.put("ta.jobs.col.title", "\u6807\u9898");
        en.put("ta.jobs.col.skills", "Required skills");
        zh.put("ta.jobs.col.skills", "\u6240\u9700\u6280\u80FD");
        en.put("ta.jobs.col.hours", "Hours/week");
        zh.put("ta.jobs.col.hours", "\u5468\u5DE5\u65F6");
        en.put("ta.jobs.col.postedby", "Posted by");
        zh.put("ta.jobs.col.postedby", "\u53D1\u5E03\u8005");
        en.put("ta.jobs.col.match", "Match Score");
        zh.put("ta.jobs.col.match", "\u5339\u914D\u5EA6");
        en.put("ta.jobs.col.tag", "Recommendation Tag");
        zh.put("ta.jobs.col.tag", "\u63A8\u8350\u6807\u7B7E");
        en.put("ta.jobs.col.reason", "Recommendation Reason");
        zh.put("ta.jobs.col.reason", "\u63A8\u8350\u7406\u7531");
        en.put("ta.jobs.loading.idle", "Ready to load recommended jobs.");
        zh.put("ta.jobs.loading.idle", "\u53EF\u4EE5\u52A0\u8F7D\u667A\u80FD\u63A8\u8350\u5C97\u4F4D\u3002");
        en.put("ta.jobs.loading", "Refreshing ranked job recommendations...");
        zh.put("ta.jobs.loading", "\u6B63\u5728\u5237\u65B0\u6392\u5E8F\u540E\u7684\u5C97\u4F4D\u63A8\u8350...");
        en.put("ta.jobs.loading.done", "{0} job(s) ranked");
        zh.put("ta.jobs.loading.done", "\u5DF2\u5B8C\u6210 {0} \u4E2A\u5C97\u4F4D\u7684\u667A\u80FD\u6392\u5E8F");
        en.put("ta.jobs.loading.ai", "Ranked jobs are ready. Streaming AI reasons for the top {0} job(s)...");
        zh.put("ta.jobs.loading.ai", "\u5C97\u4F4D\u6392\u5E8F\u5DF2\u51C6\u5907\u597D\uFF0C\u6B63\u5728\u4E3A\u524D {0} \u4E2A\u5C97\u4F4D\u6D41\u5F0F\u751F\u6210 AI \u7406\u7531...");
        en.put("ta.jobs.loading.ai.item", "Streaming AI reason {0}/{1}: {2}");
        zh.put("ta.jobs.loading.ai.item", "\u6B63\u5728\u6D41\u5F0F\u751F\u6210 AI \u7406\u7531 {0}/{1}\uFF1A{2}");
        en.put("ta.jobs.loading.ai.done", "Job ranking is ready and AI reasons for the top {0} job(s) have been updated.");
        zh.put("ta.jobs.loading.ai.done", "\u5C97\u4F4D\u6392\u5E8F\u5DF2\u5B8C\u6210\uFF0C\u524D {0} \u4E2A\u5C97\u4F4D\u7684 AI \u7406\u7531\u5DF2\u66F4\u65B0");
        en.put("ta.jobs.loading.failed", "Unable to refresh recommendations right now.");
        zh.put("ta.jobs.loading.failed", "\u6682\u65F6\u65E0\u6CD5\u5237\u65B0\u5C97\u4F4D\u63A8\u8350");
        en.put("ta.jobs.tag.profile", "Complete profile first");
        zh.put("ta.jobs.tag.profile", "\u8BF7\u5148\u5B8C\u5584\u4E2A\u4EBA\u8D44\u6599");
        en.put("ta.jobs.reason.profile", "Add skills, resume, and profile details to unlock ranked recommendations.");
        zh.put("ta.jobs.reason.profile", "\u8865\u5145\u6280\u80FD\u3001\u7B80\u5386\u548C\u4E2A\u4EBA\u63CF\u8FF0\u540E\u5373\u53EF\u83B7\u5F97\u667A\u80FD\u63A8\u8350");
        en.put("ta.jobs.detail.matched", "Matched skills:");
        zh.put("ta.jobs.detail.matched", "\u5339\u914D\u5230\u7684\u6280\u80FD\uFF1A");
        en.put("ta.jobs.detail.missing", "Missing skills:");
        zh.put("ta.jobs.detail.missing", "\u5C1A\u7F3A\u7684\u6280\u80FD\uFF1A");
        en.put("ta.jobs.detail.section.job", "Job Information");
        zh.put("ta.jobs.detail.section.job", "\u5C97\u4F4D\u4FE1\u606F");
        en.put("ta.jobs.detail.section.match", "AI Match Analysis");
        zh.put("ta.jobs.detail.section.match", "AI \u5339\u914D\u5206\u6790");
        en.put("ta.jobs.detail.section.skills", "Skills Breakdown");
        zh.put("ta.jobs.detail.section.skills", "\u6280\u80FD\u5206\u6790");
        en.put("ta.jobs.detail.section.description", "Job Description");
        zh.put("ta.jobs.detail.section.description", "\u5C97\u4F4D\u63CF\u8FF0");
        en.put("ta.jobs.detail.source", "Source: profile skills + resume analysis");
        zh.put("ta.jobs.detail.source", "\u6570\u636E\u6765\u6E90\uFF1A\u4E2A\u4EBA\u8D44\u6599\u6280\u80FD + \u7B80\u5386\u5206\u6790");

        // ── TA My Applications ──
        en.put("ta.myapps.title", "My Applications");
        zh.put("ta.myapps.title", "\u6211\u7684\u7533\u8BF7");
        en.put("ta.myapps.hint", "Only pending applications can be withdrawn");
        zh.put("ta.myapps.hint", "\u53EA\u6709\u5F85\u5BA1\u6838\u7684\u7533\u8BF7\u53EF\u4EE5\u64A4\u56DE");
        en.put("ta.myapps.confirm.withdraw", "Withdraw this application?");
        zh.put("ta.myapps.confirm.withdraw", "\u64A4\u56DE\u6B64\u7533\u8BF7\uFF1F");
        en.put("ta.myapps.cannot.withdraw", "Only pending applications can be withdrawn");
        zh.put("ta.myapps.cannot.withdraw", "\u53EA\u6709\u5F85\u5BA1\u6838\u7684\u7533\u8BF7\u53EF\u4EE5\u64A4\u56DE");
        en.put("ta.myapps.col.appid", "Application ID");
        zh.put("ta.myapps.col.appid", "\u7533\u8BF7 ID");
        en.put("ta.myapps.col.jobid", "Job ID");
        zh.put("ta.myapps.col.jobid", "\u5C97\u4F4D ID");
        en.put("ta.myapps.col.jobtitle", "Job Title");
        zh.put("ta.myapps.col.jobtitle", "\u5C97\u4F4D\u540D\u79F0");
        en.put("ta.myapps.col.status", "Status");
        zh.put("ta.myapps.col.status", "\u72B6\u6001");

        // ── TA Application Status ──
        en.put("ta.status.title", "Application Status");
        zh.put("ta.status.title", "\u7533\u8BF7\u72B6\u6001");
        en.put("ta.status.col.appid", "Application ID");
        zh.put("ta.status.col.appid", "\u7533\u8BF7 ID");
        en.put("ta.status.col.jobid", "Job ID");
        zh.put("ta.status.col.jobid", "\u5C97\u4F4D ID");
        en.put("ta.status.col.jobtitle", "Job Title");
        zh.put("ta.status.col.jobtitle", "\u5C97\u4F4D\u540D\u79F0");
        en.put("ta.status.col.status", "Status");
        zh.put("ta.status.col.status", "\u72B6\u6001");
        en.put("ta.status.col.comment", "Comment");
        zh.put("ta.status.col.comment", "\u5907\u6CE8");

        // ── MO Home ──
        en.put("mo.home.welcome", "Hi {0}, welcome to the TA recruitment system");
        zh.put("mo.home.welcome", "\u4F60\u597D {0}\uFF0C\u6B22\u8FCE\u4F7F\u7528 TA \u62DB\u8058\u7CFB\u7EDF");
        en.put("mo.home.postjob", "Post a new job");
        zh.put("mo.home.postjob", "\u53D1\u5E03\u65B0\u5C97\u4F4D");
        en.put("mo.home.pending", "Pending applicants");
        zh.put("mo.home.pending", "\u5F85\u5BA1\u7533\u8BF7\u8005");
        en.put("mo.home.stats", "Stats");
        zh.put("mo.home.stats", "\u7EDF\u8BA1");
        en.put("mo.home.stats.text", "Jobs posted: {0}   Pending applicants: {1}   Accepted: {2}");
        zh.put("mo.home.stats.text", "\u5DF2\u53D1\u5E03\u5C97\u4F4D: {0}   \u5F85\u5BA1\u7533\u8BF7\u8005: {1}   \u5DF2\u5F55\u53D6: {2}");

        // ── MO Post Job ──
        en.put("mo.post.title", "Post Job");
        zh.put("mo.post.title", "\u53D1\u5E03\u5C97\u4F4D");
        en.put("mo.post.label.title", "Title*");
        zh.put("mo.post.label.title", "\u6807\u9898*");
        en.put("mo.post.label.skills", "Required skills*");
        zh.put("mo.post.label.skills", "\u6240\u9700\u6280\u80FD*");
        en.put("mo.post.label.hours", "Hours/week*");
        zh.put("mo.post.label.hours", "\u5468\u5DE5\u65F6*");
        en.put("mo.post.label.desc", "Description*");
        zh.put("mo.post.label.desc", "\u63CF\u8FF0*");
        en.put("mo.post.hours.nan", "Hours/week must be a number");
        zh.put("mo.post.hours.nan", "\u5468\u5DE5\u65F6\u5FC5\u987B\u4E3A\u6570\u5B57");
        en.put("mo.post.hours.invalid", "Hours/week must be greater than 0");
        zh.put("mo.post.hours.invalid", "\u5468\u5DE5\u65F6\u5FC5\u987B\u5927\u4E8E 0");
        en.put("mo.post.success", "Job posted");
        zh.put("mo.post.success", "\u5C97\u4F4D\u5DF2\u53D1\u5E03");
        en.put("mo.post.ai.panel", "AI JD Assistant");
        zh.put("mo.post.ai.panel", "AI JD \u52A9\u624B");
        en.put("mo.post.ai.check", "AI Check JD");
        zh.put("mo.post.ai.check", "AI \u68C0\u67E5 JD");
        en.put("mo.post.ai.polish", "AI Polish");
        zh.put("mo.post.ai.polish", "AI \u6DA6\u8272");
        en.put("mo.post.ai.idle", "Ready to review or polish the current draft.");
        zh.put("mo.post.ai.idle", "\u53EF\u4EE5\u5BF9\u5F53\u524D JD \u8349\u7A3F\u8FDB\u884C\u68C0\u67E5\u6216\u6DA6\u8272\u3002");
        en.put("mo.post.ai.initial", "Use AI Check JD to review requirement quality, clarity, and missing information. Use AI Polish to generate a cleaner version that you can apply back into the form.");
        zh.put("mo.post.ai.initial", "\u4F7F\u7528 AI \u68C0\u67E5 JD \u53EF\u4EE5\u8BC4\u4F30\u6280\u80FD\u8981\u6C42\u662F\u5426\u5408\u7406\u3001\u63CF\u8FF0\u662F\u5426\u6E05\u6670\u3001\u662F\u5426\u7F3A\u5C11\u5173\u952E\u4FE1\u606F\u3002\u4F7F\u7528 AI \u6DA6\u8272\u53EF\u4EE5\u751F\u6210\u53EF\u76F4\u63A5\u56DE\u586B\u5230\u8868\u5355\u7684\u4F18\u5316\u7248\u672C\u3002");
        en.put("mo.post.ai.checking", "AI is reviewing the JD draft...");
        zh.put("mo.post.ai.checking", "AI \u6B63\u5728\u68C0\u67E5 JD \u8349\u7A3F...");
        en.put("mo.post.ai.polishing", "AI is polishing the JD draft...");
        zh.put("mo.post.ai.polishing", "AI \u6B63\u5728\u6DA6\u8272 JD \u8349\u7A3F...");
        en.put("mo.post.ai.pending.review", "Checking skill reasonableness, description clarity, and missing information...");
        zh.put("mo.post.ai.pending.review", "\u6B63\u5728\u68C0\u67E5\u6280\u80FD\u8981\u6C42\u5408\u7406\u6027\u3001\u63CF\u8FF0\u6E05\u6670\u5EA6\u548C\u5173\u952E\u4FE1\u606F\u7F3A\u5931\u60C5\u51B5...");
        en.put("mo.post.ai.pending.polish", "Generating a polished version based on the current draft...");
        zh.put("mo.post.ai.pending.polish", "\u6B63\u5728\u57FA\u4E8E\u5F53\u524D JD \u8349\u7A3F\u751F\u6210\u6DA6\u8272\u7248...");
        en.put("mo.post.ai.check.done", "AI review complete (Score: {0}/100)");
        zh.put("mo.post.ai.check.done", "AI \u68C0\u67E5\u5B8C\u6210\uFF08\u8BC4\u5206\uFF1A{0}/100\uFF09");
        en.put("mo.post.ai.polish.done", "AI polished draft is ready");
        zh.put("mo.post.ai.polish.done", "AI \u6DA6\u8272\u7248\u5DF2\u751F\u6210");
        en.put("mo.post.ai.applied", "Polished draft applied to the form");
        zh.put("mo.post.ai.applied", "\u6DA6\u8272\u7248\u5185\u5BB9\u5DF2\u5E94\u7528\u5230\u8868\u5355");
        en.put("mo.post.ai.apply", "Apply to Form");
        zh.put("mo.post.ai.apply", "\u5E94\u7528\u5230\u8868\u5355");
        en.put("mo.post.ai.apply.all", "Apply All");
        zh.put("mo.post.ai.apply.all", "\u5168\u90E8\u5E94\u7528");
        en.put("mo.post.ai.apply.title", "Title Only");
        zh.put("mo.post.ai.apply.title", "\u4EC5\u5E94\u7528\u6807\u9898");
        en.put("mo.post.ai.apply.skills", "Skills Only");
        zh.put("mo.post.ai.apply.skills", "\u4EC5\u5E94\u7528\u6280\u80FD");
        en.put("mo.post.ai.apply.description", "Description Only");
        zh.put("mo.post.ai.apply.description", "\u4EC5\u5E94\u7528\u63CF\u8FF0");
        en.put("mo.post.ai.unconfigured", "AI features require DeepSeek configuration. Please set api_key in {0}.");
        zh.put("mo.post.ai.unconfigured", "AI \u529F\u80FD\u9700\u8981\u914D\u7F6E DeepSeek\u3002\u8BF7\u5728 {0} \u4E2D\u586B\u5199 api_key\u3002");
        en.put("mo.post.ai.failed", "AI request failed");
        zh.put("mo.post.ai.failed", "AI \u8BF7\u6C42\u5931\u8D25");
        en.put("mo.post.ai.failed.detail", "AI request failed: {0}");
        zh.put("mo.post.ai.failed.detail", "AI \u8BF7\u6C42\u5931\u8D25\uFF1A{0}");
        en.put("mo.post.ai.review.score", "Overall Score");
        zh.put("mo.post.ai.review.score", "\u603B\u4F53\u8BC4\u5206");
        en.put("mo.post.ai.review.summary", "Overall Assessment");
        zh.put("mo.post.ai.review.summary", "\u603B\u4F53\u8BC4\u4EF7");
        en.put("mo.post.ai.review.issues", "Structured Issue List");
        zh.put("mo.post.ai.review.issues", "\u7ED3\u6784\u5316\u95EE\u9898\u6E05\u5355");
        en.put("mo.post.ai.review.skills", "Skill Requirement Reasonableness");
        zh.put("mo.post.ai.review.skills", "\u6280\u80FD\u8981\u6C42\u5408\u7406\u6027");
        en.put("mo.post.ai.review.clarity", "Description Clarity");
        zh.put("mo.post.ai.review.clarity", "\u63CF\u8FF0\u6E05\u6670\u5EA6");
        en.put("mo.post.ai.review.missing", "Missing Key Information");
        zh.put("mo.post.ai.review.missing", "\u7F3A\u5C11\u7684\u5173\u952E\u4FE1\u606F");
        en.put("mo.post.ai.review.suggestions", "Improvement Suggestions");
        zh.put("mo.post.ai.review.suggestions", "\u6539\u8FDB\u5EFA\u8BAE");
        en.put("mo.post.ai.polish.preview", "AI Polished Draft");
        zh.put("mo.post.ai.polish.preview", "AI \u6DA6\u8272\u7248 JD");
        en.put("mo.post.ai.polish.changes", "Change Summary");
        zh.put("mo.post.ai.polish.changes", "\u4F18\u5316\u8BF4\u660E");

        // ── MO Applicants ──
        en.put("mo.applicants.title", "Applicants");
        zh.put("mo.applicants.title", "\u7533\u8BF7\u8005");
        en.put("mo.applicants.opencv", "Open CV");
        zh.put("mo.applicants.opencv", "\u6253\u5F00\u7B80\u5386");
        en.put("mo.applicants.viewdetails", "View Details");
        zh.put("mo.applicants.viewdetails", "\u67E5\u770B\u8BE6\u60C5");
        en.put("mo.applicants.nocv", "No CV available for this applicant");
        zh.put("mo.applicants.nocv", "\u8BE5\u7533\u8BF7\u8005\u65E0\u53EF\u7528\u7B80\u5386");
        en.put("mo.applicants.cv.openfailed", "Unable to open CV file: ");
        zh.put("mo.applicants.cv.openfailed", "\u65E0\u6CD5\u6253\u5F00\u7B80\u5386\u6587\u4EF6: ");
        en.put("mo.applicants.batch.confirm", "Are you sure you want to {0} {1} applicant(s)?");
        zh.put("mo.applicants.batch.confirm", "\u786E\u5B9A\u8981 {0} {1} \u4E2A\u7533\u8BF7\u8005\u5417\uFF1F");
        en.put("mo.applicants.batch.title", "Confirm Batch Operation");
        zh.put("mo.applicants.batch.title", "\u786E\u8BA4\u6279\u91CF\u64CD\u4F5C");
        en.put("mo.applicants.batch.multiselect", "Please select one or more applicants (hold Ctrl or Shift to multi-select)");
        zh.put("mo.applicants.batch.multiselect", "\u8BF7\u9009\u62E9\u4E00\u4E2A\u6216\u591A\u4E2A\u7533\u8BF7\u8005\uFF08\u6309\u4F4F Ctrl \u6216 Shift \u591A\u9009\uFF09");
        en.put("mo.applicants.details.title", "Applicant Details");
        zh.put("mo.applicants.details.title", "\u7533\u8BF7\u8005\u8BE6\u60C5");
        en.put("mo.applicants.col.appid", "Application ID");
        zh.put("mo.applicants.col.appid", "\u7533\u8BF7 ID");
        en.put("mo.applicants.col.taaccount", "TA Account");
        zh.put("mo.applicants.col.taaccount", "TA \u8D26\u53F7");
        en.put("mo.applicants.col.taname", "TA Name");
        zh.put("mo.applicants.col.taname", "TA \u59D3\u540D");
        en.put("mo.applicants.col.email", "Email");
        zh.put("mo.applicants.col.email", "\u90AE\u7BB1");
        en.put("mo.applicants.col.skills", "Skills");
        zh.put("mo.applicants.col.skills", "\u6280\u80FD");
        en.put("mo.applicants.col.match", "Match %");
        zh.put("mo.applicants.col.match", "\u5339\u914D %");
        en.put("mo.applicants.col.tag", "Recommendation Tag");
        zh.put("mo.applicants.col.tag", "\u63A8\u8350\u6807\u7B7E");
        en.put("mo.applicants.col.status", "Status");
        zh.put("mo.applicants.col.status", "\u72B6\u6001");
        en.put("mo.applicants.ai.explain", "AI Explain Match");
        zh.put("mo.applicants.ai.explain", "AI \u89E3\u91CA\u5339\u914D");
        en.put("mo.applicants.loading.idle", "Choose a job to load applicants.");
        zh.put("mo.applicants.loading.idle", "\u8BF7\u9009\u62E9\u4E00\u4E2A\u5C97\u4F4D\u4EE5\u52A0\u8F7D\u7533\u8BF7\u8005");
        en.put("mo.applicants.loading.empty", "No job selected.");
        zh.put("mo.applicants.loading.empty", "\u5F53\u524D\u672A\u9009\u4E2D\u5C97\u4F4D");
        en.put("mo.applicants.loading", "Loading applicants with fast local scoring...");
        zh.put("mo.applicants.loading", "\u6B63\u5728\u4F7F\u7528\u672C\u5730\u5FEB\u901F\u8BC4\u5206\u52A0\u8F7D\u7533\u8BF7\u8005...");
        en.put("mo.applicants.loading.done", "{0} applicant(s) loaded with fast scoring.");
        zh.put("mo.applicants.loading.done", "\u5DF2\u52A0\u8F7D {0} \u4E2A\u7533\u8BF7\u8005\u5E76\u5B8C\u6210\u5FEB\u901F\u8BC4\u5206");
        en.put("mo.applicants.loading.failed", "Unable to load applicants right now.");
        zh.put("mo.applicants.loading.failed", "\u6682\u65F6\u65E0\u6CD5\u52A0\u8F7D\u7533\u8BF7\u8005");
        en.put("mo.applicants.ai.streaming", "Streaming AI match explanation...");
        zh.put("mo.applicants.ai.streaming", "\u6B63\u5728\u6D41\u5F0F\u751F\u6210 AI \u5339\u914D\u89E3\u91CA...");
        en.put("mo.applicants.ai.streaming.start", "AI explanation stream is starting...\n\n");
        zh.put("mo.applicants.ai.streaming.start", "AI \u89E3\u91CA\u6D41\u5F0F\u8F93\u51FA\u5F00\u59CB...\n\n");
        en.put("mo.applicants.ai.streaming.done", "AI match explanation is ready.");
        zh.put("mo.applicants.ai.streaming.done", "AI \u5339\u914D\u89E3\u91CA\u5DF2\u751F\u6210");
        en.put("mo.applicants.detail.overall", "Overall score");
        zh.put("mo.applicants.detail.overall", "\u603B\u5206");
        en.put("mo.applicants.detail.skill", "Skill score");
        zh.put("mo.applicants.detail.skill", "\u6280\u80FD\u5206");
        en.put("mo.applicants.detail.seniority", "Seniority score");
        zh.put("mo.applicants.detail.seniority", "\u7ECF\u9A8C\u5C42\u7EA7\u5206");
        en.put("mo.applicants.detail.domain", "Domain score");
        zh.put("mo.applicants.detail.domain", "\u9886\u57DF\u5206");
        en.put("mo.applicants.detail.matched", "Matched skills");
        zh.put("mo.applicants.detail.matched", "\u5339\u914D\u6280\u80FD");
        en.put("mo.applicants.detail.missing", "Missing skills");
        zh.put("mo.applicants.detail.missing", "\u7F3A\u5931\u6280\u80FD");
        en.put("mo.applicants.detail.reason", "Recommendation reason");
        zh.put("mo.applicants.detail.reason", "\u63A8\u8350\u7406\u7531");
        en.put("mo.applicants.detail.source", "Source: profile skills + resume analysis");
        zh.put("mo.applicants.detail.source", "\u6570\u636E\u6765\u6E90\uFF1A\u4E2A\u4EBA\u8D44\u6599\u6280\u80FD + \u7B80\u5386\u5206\u6790");

        // ── MO Results ──
        en.put("mo.results.title", "Results");
        zh.put("mo.results.title", "\u7ED3\u679C");
        en.put("mo.results.send", "Send notice (placeholder)");
        zh.put("mo.results.send", "\u53D1\u9001\u901A\u77E5\uFF08\u5360\u4F4D\uFF09");
        en.put("mo.results.col.appid", "Application ID");
        zh.put("mo.results.col.appid", "\u7533\u8BF7 ID");
        en.put("mo.results.col.taaccount", "TA Name");
        zh.put("mo.results.col.taaccount", "TA \u59D3\u540D");
        en.put("mo.results.col.status", "Status");
        zh.put("mo.results.col.status", "\u72B6\u6001");

        // ── MO My Posts ──
        en.put("mo.myposts.title", "My Posts");
        zh.put("mo.myposts.title", "\u6211\u7684\u53D1\u5E03");
        en.put("mo.myposts.hint", "Showing jobs posted by you");
        zh.put("mo.myposts.hint", "\u663E\u793A\u60A8\u53D1\u5E03\u7684\u5C97\u4F4D");
        en.put("mo.myposts.col.id", "Job ID");
        zh.put("mo.myposts.col.id", "\u5C97\u4F4D ID");
        en.put("mo.myposts.col.title", "Title");
        zh.put("mo.myposts.col.title", "\u6807\u9898");
        en.put("mo.myposts.col.hours", "Hours/week");
        zh.put("mo.myposts.col.hours", "\u5468\u5DE5\u65F6");
        en.put("mo.myposts.col.skills", "Required skills");
        zh.put("mo.myposts.col.skills", "\u6240\u9700\u6280\u80FD");

        // ── Admin Home ──
        en.put("admin.home.overview", "System Overview");
        zh.put("admin.home.overview", "\u7CFB\u7EDF\u6982\u89C8");
        en.put("admin.home.stats", "Stats");
        zh.put("admin.home.stats", "\u7EDF\u8BA1");
        en.put("admin.home.stats.text", "TA registered: {0}   Accounts: {1}   Jobs: {2}");
        zh.put("admin.home.stats.text", "TA \u6CE8\u518C: {0}   \u8D26\u53F7: {1}   \u5C97\u4F4D: {2}");

        // ── Admin User Management ──
        en.put("admin.users.title", "User Management");
        zh.put("admin.users.title", "\u7528\u6237\u7BA1\u7406");
        en.put("admin.users.rolefilter", "Role filter:");
        zh.put("admin.users.rolefilter", "\u89D2\u8272\u7B5B\u9009\uFF1A");
        en.put("admin.users.addmo", "Add MO Account");
        zh.put("admin.users.addmo", "\u6DFB\u52A0 MO \u8D26\u53F7");
        en.put("admin.users.addmo.title", "Add MO Account");
        zh.put("admin.users.addmo.title", "\u6DFB\u52A0 MO \u8D26\u53F7");
        en.put("admin.users.staffid", "Staff ID (Account)*");
        zh.put("admin.users.staffid", "\u5DE5\u53F7\uFF08\u8D26\u53F7\uFF09*");
        en.put("admin.users.password", "Password*");
        zh.put("admin.users.password", "\u5BC6\u7801*");
        en.put("admin.users.name", "Name (optional)");
        zh.put("admin.users.name", "\u59D3\u540D\uFF08\u53EF\u9009\uFF09");
        en.put("admin.users.only.tamo", "Only TA/MO users are supported");
        zh.put("admin.users.only.tamo", "\u4EC5\u652F\u6301 TA/MO \u7528\u6237");
        en.put("admin.users.confirm.delete", "This cannot be undone. Delete this account?");
        zh.put("admin.users.confirm.delete", "\u6B64\u64CD\u4F5C\u4E0D\u53EF\u64A4\u9500\u3002\u5220\u9664\u6B64\u8D26\u53F7\uFF1F");
        en.put("admin.users.col.role", "Role");
        zh.put("admin.users.col.role", "\u89D2\u8272");
        en.put("admin.users.col.account", "Account");
        zh.put("admin.users.col.account", "\u8D26\u53F7");
        en.put("admin.users.col.name", "Name");
        zh.put("admin.users.col.name", "\u59D3\u540D");
        en.put("admin.users.col.status", "Status");
        zh.put("admin.users.col.status", "\u72B6\u6001");
        en.put("admin.users.enabled", "Enabled");
        zh.put("admin.users.enabled", "\u5DF2\u542F\u7528");
        en.put("admin.users.disabled", "Disabled");
        zh.put("admin.users.disabled", "\u5DF2\u7981\u7528");

        // ── Admin Config ──
        en.put("admin.config.title", "System Config");
        zh.put("admin.config.title", "\u7CFB\u7EDF\u914D\u7F6E");
        en.put("admin.config.datapath", "Data path (effective next startup):");
        zh.put("admin.config.datapath", "\u6570\u636E\u8DEF\u5F84\uFF08\u4E0B\u6B21\u542F\u52A8\u751F\u6548\uFF09\uFF1A");
        en.put("admin.config.pwdlen", "Password min length (TA register):");
        zh.put("admin.config.pwdlen", "\u5BC6\u7801\u6700\u5C0F\u957F\u5EA6\uFF08TA \u6CE8\u518C\uFF09\uFF1A");
        en.put("admin.config.cvformats", "CV formats (comma-separated):");
        zh.put("admin.config.cvformats", "\u7B80\u5386\u683C\u5F0F\uFF08\u9017\u53F7\u5206\u9694\uFF09\uFF1A");
        en.put("admin.config.lang", "Default language (EN):");
        zh.put("admin.config.lang", "\u9ED8\u8BA4\u8BED\u8A00 (EN)\uFF1A");
        en.put("admin.config.pwdlen.nan", "Password min length must be a number");
        zh.put("admin.config.pwdlen.nan", "\u5BC6\u7801\u6700\u5C0F\u957F\u5EA6\u5FC5\u987B\u4E3A\u6570\u5B57");
        en.put("admin.config.datapath.hint", "Leave blank to use the default ./data directory");
        zh.put("admin.config.datapath.hint", "\u7559\u7a7a\u5219\u4f7f\u7528\u9ed8\u8ba4 ./data \u76ee\u5f55");
        en.put("admin.config.saved", "Saved. Data path changes take effect on next startup.");
        zh.put("admin.config.saved", "\u5DF2\u4FDD\u5B58\u3002\u6570\u636E\u8DEF\u5F84\u66F4\u6539\u5C06\u5728\u4E0B\u6B21\u542F\u52A8\u65F6\u751F\u6548\u3002");

        // ── Admin Export ──
        en.put("admin.export.title", "Data Export");
        zh.put("admin.export.title", "\u6570\u636E\u5BFC\u51FA");
        en.put("admin.export.type", "Type:");
        zh.put("admin.export.type", "\u7C7B\u578B\uFF1A");
        en.put("admin.export.format", "Format:");
        zh.put("admin.export.format", "\u683C\u5F0F\uFF1A");
        en.put("admin.export.preview", "Preview (shows first ~50 lines of what will be exported)");
        zh.put("admin.export.preview", "\u9884\u89C8\uFF08\u663E\u793A\u5BFC\u51FA\u5185\u5BB9\u7684\u524D ~50 \u884C\uFF09");

        // ── Admin Logs ──
        en.put("admin.logs.title", "Operation Logs");
        zh.put("admin.logs.title", "\u64CD\u4F5C\u65E5\u5FD7");
        en.put("admin.logs.actor", "Actor:");
        zh.put("admin.logs.actor", "\u64CD\u4F5C\u4EBA\uFF1A");
        en.put("admin.logs.action", "Action:");
        zh.put("admin.logs.action", "\u64CD\u4F5C\uFF1A");
        en.put("admin.logs.level", "Level:");
        zh.put("admin.logs.level", "\u7EA7\u522B\uFF1A");
        en.put("admin.logs.keyword", "Keyword:");
        zh.put("admin.logs.keyword", "\u5173\u952E\u5B57\uFF1A");
        en.put("admin.logs.confirm.clear", "Clear all logs?");
        zh.put("admin.logs.confirm.clear", "\u6E05\u7A7A\u6240\u6709\u65E5\u5FD7\uFF1F");
        en.put("admin.logs.export.title", "Export Logs");
        zh.put("admin.logs.export.title", "\u5BFC\u51FA\u65E5\u5FD7");

        // ── Admin Workload ──
        en.put("admin.workload.title", "TA Workload");
        zh.put("admin.workload.title", "TA \u5DE5\u4F5C\u91CF");
        en.put("admin.workload.view", "View:");
        zh.put("admin.workload.view", "\u89C6\u56FE\uFF1A");
        en.put("admin.workload.from", "From (yyyy-MM-dd):");
        zh.put("admin.workload.from", "\u8D77\u59CB (yyyy-MM-dd)\uFF1A");
        en.put("admin.workload.to", "To (yyyy-MM-dd):");
        zh.put("admin.workload.to", "\u622A\u6B62 (yyyy-MM-dd)\uFF1A");
        en.put("admin.workload.category", "Category:");
        zh.put("admin.workload.category", "\u5206\u7C7B\uFF1A");
        en.put("admin.workload.ai", "AI Balancing (placeholder)");
        zh.put("admin.workload.ai", "AI \u5747\u8861\uFF08\u5360\u4F4D\uFF09");
        en.put("admin.workload.ai.msg", "Placeholder: AI workload balancing/ratings will be added later.");
        zh.put("admin.workload.ai.msg", "\u5360\u4F4D\uFF1AAI \u5DE5\u4F5C\u91CF\u5747\u8861/\u8BC4\u5206\u529F\u80FD\u5C06\u5728\u540E\u7EED\u7248\u672C\u6DFB\u52A0\u3002");
        en.put("admin.workload.col.key", "Key");
        zh.put("admin.workload.col.key", "\u6807\u8BC6");
        en.put("admin.workload.col.name", "Name/Title");
        zh.put("admin.workload.col.name", "\u59D3\u540D/\u6807\u9898");
        en.put("admin.workload.col.accepted", "Accepted");
        zh.put("admin.workload.col.accepted", "\u5DF2\u5F55\u53D6");
        en.put("admin.workload.col.hours", "Total hours");
        zh.put("admin.workload.col.hours", "\u603B\u5DE5\u65F6");
        en.put("admin.workload.export.title", "Export Workload CSV");
        zh.put("admin.workload.export.title", "\u5BFC\u51FA\u5DE5\u4F5C\u91CF CSV");

        // ── Admin Job Data ──
        en.put("admin.jobdata.title", "Job Data");
        zh.put("admin.jobdata.title", "\u5C97\u4F4D\u6570\u636E");
        en.put("admin.jobdata.keyword", "Keyword:");
        zh.put("admin.jobdata.keyword", "\u5173\u952E\u5B57\uFF1A");
        en.put("admin.jobdata.postedby", "Posted by:");
        zh.put("admin.jobdata.postedby", "\u53D1\u5E03\u8005\uFF1A");
        en.put("admin.jobdata.status", "Status:");
        zh.put("admin.jobdata.status", "\u72B6\u6001\uFF1A");
        en.put("admin.jobdata.category", "Category:");
        zh.put("admin.jobdata.category", "\u5206\u7C7B\uFF1A");
        en.put("admin.jobdata.forceclose", "Force Close");
        zh.put("admin.jobdata.forceclose", "\u5F3A\u5236\u5173\u95ED");
        en.put("admin.jobdata.complete", "Mark Completed");
        zh.put("admin.jobdata.complete", "\u6807\u8BB0\u5B8C\u6210");
        en.put("admin.jobdata.setcategory", "Set Category");
        zh.put("admin.jobdata.setcategory", "\u8BBE\u7F6E\u5206\u7C7B");
        en.put("admin.jobdata.categoryprompt", "Category:");
        zh.put("admin.jobdata.categoryprompt", "\u5206\u7C7B\uFF1A");
        en.put("admin.jobdata.col.id", "Job ID");
        zh.put("admin.jobdata.col.id", "\u5C97\u4F4D ID");
        en.put("admin.jobdata.col.postedby", "Posted by");
        zh.put("admin.jobdata.col.postedby", "\u53D1\u5E03\u8005");
        en.put("admin.jobdata.col.title", "Title");
        zh.put("admin.jobdata.col.title", "\u6807\u9898");
        en.put("admin.jobdata.col.hours", "Hours/week");
        zh.put("admin.jobdata.col.hours", "\u5468\u5DE5\u65F6");
        en.put("admin.jobdata.col.status", "Status");
        zh.put("admin.jobdata.col.status", "\u72B6\u6001");
        en.put("admin.jobdata.col.category", "Category");
        zh.put("admin.jobdata.col.category", "\u5206\u7C7B");
        en.put("admin.jobdata.col.apps", "Applications");
        zh.put("admin.jobdata.col.apps", "\u7533\u8BF7\u6570");
        en.put("admin.jobdata.col.accepted", "Accepted");
        zh.put("admin.jobdata.col.accepted", "\u5DF2\u5F55\u53D6");

        // ── AppLayout ──
        en.put("layout.profile", "Profile");
        zh.put("layout.profile", "\u4E2A\u4EBA\u8D44\u6599");
        en.put("layout.settings", "Settings");
        zh.put("layout.settings", "\u8BBE\u7F6E");
        en.put("layout.language", "Language");
        zh.put("layout.language", "\u8BED\u8A00");
        en.put("layout.notifications", "Notifications");
        zh.put("layout.notifications", "\u901A\u77E5");
        en.put("layout.notifications.unread", "Notifications ({0} unread)");
        zh.put("layout.notifications.unread", "\u901A\u77E5\uFF08{0} \u672A\u8BFB\uFF09");
        en.put("layout.notifications.hasunread", "You have {0} unread notification(s).");
        zh.put("layout.notifications.hasunread", "\u60A8\u6709 {0} \u6761\u672A\u8BFB\u901A\u77E5\u3002");
        en.put("layout.notifications.none", "No new notifications.");
        zh.put("layout.notifications.none", "\u6CA1\u6709\u65B0\u901A\u77E5\u3002");
        en.put("layout.notifications.empty", "No notifications.");
        zh.put("layout.notifications.empty", "\u6CA1\u6709\u901A\u77E5\u3002");

        // -- Notification content --
        en.put("notif.header", "Recent Activity");
        zh.put("notif.header", "\u6700\u8FD1\u52A8\u6001");
        en.put("notif.action.submitApplication", "submitted an application");
        zh.put("notif.action.submitApplication", "\u63D0\u4EA4\u4E86\u7533\u8BF7");
        en.put("notif.action.setApplicationStatus", "updated an application status");
        zh.put("notif.action.setApplicationStatus", "\u66F4\u65B0\u4E86\u7533\u8BF7\u72B6\u6001");
        en.put("notif.action.withdrawApplication", "withdrew an application");
        zh.put("notif.action.withdrawApplication", "\u64A4\u56DE\u4E86\u7533\u8BF7");
        en.put("notif.action.createJob", "posted a new job");
        zh.put("notif.action.createJob", "\u53D1\u5E03\u4E86\u65B0\u5C97\u4F4D");
        en.put("notif.action.setJobStatus", "changed a job status");
        zh.put("notif.action.setJobStatus", "\u66F4\u6539\u4E86\u5C97\u4F4D\u72B6\u6001");
        en.put("notif.action.setJobCategory", "set a job category");
        zh.put("notif.action.setJobCategory", "\u8BBE\u7F6E\u4E86\u5C97\u4F4D\u5206\u7C7B");
        en.put("notif.action.setUserEnabled", "changed user account status");
        zh.put("notif.action.setUserEnabled", "\u66F4\u6539\u4E86\u7528\u6237\u8D26\u53F7\u72B6\u6001");
        en.put("notif.action.deleteUser", "deleted a user account");
        zh.put("notif.action.deleteUser", "\u5220\u9664\u4E86\u7528\u6237\u8D26\u53F7");
        en.put("notif.action.resetPassword", "reset a password");
        zh.put("notif.action.resetPassword", "\u91CD\u7F6E\u4E86\u5BC6\u7801");
        en.put("notif.action.updateConfig", "updated system configuration");
        zh.put("notif.action.updateConfig", "\u66F4\u65B0\u4E86\u7CFB\u7EDF\u914D\u7F6E");
        en.put("notif.action.createMoAccount", "created an MO account");
        zh.put("notif.action.createMoAccount", "\u521B\u5EFA\u4E86 MO \u8D26\u53F7");
        en.put("notif.action.export", "exported data");
        zh.put("notif.action.export", "\u5BFC\u51FA\u4E86\u6570\u636E");
        en.put("notif.action.exportWorkloadCsv", "exported workload CSV");
        zh.put("notif.action.exportWorkloadCsv", "\u5BFC\u51FA\u4E86\u5DE5\u4F5C\u91CF CSV");
        en.put("notif.action.resetApplicantAiScores", "reset AI scores for an applicant");
        zh.put("notif.action.resetApplicantAiScores", "\u91CD\u7F6E\u4E86\u7533\u8BF7\u4EBA\u7684 AI \u8BC4\u5206");
        en.put("notif.detail.title", "Title");
        zh.put("notif.detail.title", "\u6807\u9898");
        en.put("notif.detail.hours", "Hours/week");
        zh.put("notif.detail.hours", "\u5468\u5DE5\u65F6");
        en.put("notif.detail.skills", "Skills");
        zh.put("notif.detail.skills", "\u6280\u80FD");
        en.put("notif.detail.jobId", "Job ID");
        zh.put("notif.detail.jobId", "\u5C97\u4F4D ID");
        en.put("notif.detail.postedBy", "Posted by");
        zh.put("notif.detail.postedBy", "\u53D1\u5E03\u8005");
        en.put("notif.status.SUBMITTED", "SUBMITTED");
        zh.put("notif.status.SUBMITTED", "\u5DF2\u63D0\u4EA4");
        en.put("notif.status.ACCEPTED", "ACCEPTED");
        zh.put("notif.status.ACCEPTED", "\u5DF2\u5F55\u53D6");
        en.put("notif.status.REJECTED", "REJECTED");
        zh.put("notif.status.REJECTED", "\u5DF2\u62D2\u7EDD");
        en.put("notif.submit.detail", "A new candidate application was submitted for review.");
        zh.put("notif.submit.detail", "\u6536\u5230\u4E00\u4EFD\u65B0\u7684\u5019\u9009\u4EBA\u7533\u8BF7\u3002");
        en.put("notif.withdraw.detail", "The candidate withdrew this application.");
        zh.put("notif.withdraw.detail", "\u5019\u9009\u4EBA\u5DF2\u64A4\u56DE\u6B64\u7533\u8BF7\u3002");
        en.put("notif.admin.setUserEnabled.enabled", "Enabled user account {0}");
        zh.put("notif.admin.setUserEnabled.enabled", "\u5DF2\u542F\u7528\u7528\u6237\u8D26\u53F7 {0}");
        en.put("notif.admin.setUserEnabled.disabled", "Disabled user account {0}");
        zh.put("notif.admin.setUserEnabled.disabled", "\u5DF2\u7981\u7528\u7528\u6237\u8D26\u53F7 {0}");
        en.put("notif.admin.deleteUser", "Deleted {0} account {1}");
        zh.put("notif.admin.deleteUser", "\u5DF2\u5220\u9664 {0} \u8D26\u53F7 {1}");
        en.put("notif.admin.resetPassword", "Reset password for {0} account {1}");
        zh.put("notif.admin.resetPassword", "\u5DF2\u91CD\u7F6E {0} \u8D26\u53F7 {1} \u7684\u5BC6\u7801");
        en.put("notif.admin.createMoAccount", "Created MO account {0}");
        zh.put("notif.admin.createMoAccount", "\u5DF2\u521B\u5EFA MO \u8D26\u53F7 {0}");
        en.put("notif.admin.export", "Exported {0} data ({1}) to {2}");
        zh.put("notif.admin.export", "\u5DF2\u5BFC\u51FA {0} \u6570\u636E ({1}) \u81F3 {2}");
        en.put("notif.admin.exportWorkloadCsv", "Exported workload CSV to {0}");
        zh.put("notif.admin.exportWorkloadCsv", "\u5DF2\u5BFC\u51FA\u5DE5\u4F5C\u91CF CSV \u81F3 {0}");
        en.put("notif.admin.setJobStatus", "Set job {0} status to");
        zh.put("notif.admin.setJobStatus", "\u5DF2\u5C06\u5C97\u4F4D {0} \u72B6\u6001\u8BBE\u4E3A");
        en.put("notif.admin.setJobCategory", "Set job {0} category to {1}");
        zh.put("notif.admin.setJobCategory", "\u5DF2\u5C06\u5C97\u4F4D {0} \u5206\u7C7B\u8BBE\u4E3A {1}");
        en.put("notif.admin.resetApplicantAiScores", "Reset AI scores for applicant {0}");
        zh.put("notif.admin.resetApplicantAiScores", "\u5DF2\u91CD\u7F6E\u7533\u8BF7\u4EBA {0} \u7684 AI \u8BC4\u5206");
        en.put("notif.admin.updateConfig", "Updated system configuration");
        zh.put("notif.admin.updateConfig", "\u5DF2\u66F4\u65B0\u7CFB\u7EDF\u914D\u7F6E");
        en.put("notif.admin.updateConfig.detail", "Data path: {0}, Min password length: {1}, CV formats: {2}, Default language: {3}");
        zh.put("notif.admin.updateConfig.detail", "\u6570\u636E\u8DEF\u5F84: {0}, \u5BC6\u7801\u6700\u77ED\u957F\u5EA6: {1}, \u7B80\u5386\u683C\u5F0F: {2}, \u9ED8\u8BA4\u8BED\u8A00: {3}");
        en.put("notif.admin.error", "System Error");
        zh.put("notif.admin.error", "\u7CFB\u7EDF\u9519\u8BEF");
        en.put("notif.admin.warn", "System Warning");
        zh.put("notif.admin.warn", "\u7CFB\u7EDF\u8B66\u544A");
        en.put("notif.admin.performed", "Performed: {0}");
        zh.put("notif.admin.performed", "\u6267\u884C\u4E86: {0}");

        // ── ForgotPassword ──
        en.put("forgot.role", "Role");
        zh.put("forgot.role", "\u89D2\u8272");

        // ── LoginPanel ──
        en.put("login.placeholder.account", "Please enter your Student/Staff ID");
        zh.put("login.placeholder.account", "\u8BF7\u8F93\u5165\u5B66\u53F7/\u5DE5\u53F7");
        en.put("login.placeholder.password", "Please enter your password");
        zh.put("login.placeholder.password", "\u8BF7\u8F93\u5165\u5BC6\u7801");
        en.put("login.footer", "BUPT International School \u00B7 TA Recruitment System");
        zh.put("login.footer", "\u5317\u90AE\u56FD\u9645\u5B66\u9662 \u00B7 TA \u62DB\u8058\u7CFB\u7EDF");
        en.put("msg.password.tooshort", "Password must be at least {0} characters");
        zh.put("msg.password.tooshort", "\u5BC6\u7801\u81F3\u5C11\u9700\u8981 {0} \u4E2A\u5B57\u7B26");
        en.put("msg.agree.required", "Please accept the registration terms");
        zh.put("msg.agree.required", "\u8BF7\u63A5\u53D7\u6CE8\u518C\u6761\u6B3E");
        en.put("msg.account.exists", "Account already exists");
        zh.put("msg.account.exists", "\u8D26\u53F7\u5DF2\u5B58\u5728");
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

    public static String t(String key, Object... args) {
        String tpl = t(key);
        if (args == null || args.length == 0) return tpl;
        for (int i = 0; i < args.length; i++) {
            tpl = tpl.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return tpl;
    }
}
