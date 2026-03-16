package ebu6304.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ebu6304.storage.DataService;
import ebu6304.ui.StatusBar;
import ebu6304.ui.I18n;

public final class MainFrame extends JFrame {
    private final DataService data;
    private final CardLayout cards = new CardLayout();
    private final JPanel container = new JPanel(cards);
    private final StatusBar statusBar = new StatusBar();

    public MainFrame(DataService data) {
        super(I18n.t("app.title"));
        this.data = data;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1100, 720));

        buildScreens();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(container, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        statusBar.setLeftText(I18n.t("status.ready"));
        statusBar.setRightText(data.dataDir().toAbsolutePath().toString());

        pack();
        setLocationRelativeTo(null);
    }

    private void buildScreens() {
        StartPanel start = new StartPanel(() -> show("login"));

        LoginPanel login = new LoginPanel(data, new LoginPanel.LoginHandler() {
            @Override
            public void onLogin(Role role, String account) {
                showWorkbench(role, account);
            }

            @Override
            public void onForgotPassword() {
                show("forgot");
            }
        });

        ForgotPasswordPanel forgot = new ForgotPasswordPanel(data, () -> show("login"));

        container.add(start, "start");
        container.add(login, "login");
        container.add(forgot, "forgot");

        show("start");
    }

    private void showWorkbench(Role role, String account) {
        WorkbenchPanel wb = new WorkbenchPanel(data, role, account, () -> show("login"));
        container.add(wb, "workbench");
        show("workbench");
    }

    private void show(String key) {
        cards.show(container, key);
    }
}
