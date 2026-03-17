import javax.swing.*;
import java.awt.*;
import javax.swing.BorderFactory;

public class signup_page extends JFrame {
    private JPanel MainCPanel;
    private JPanel CreationPanel;
    private JLabel TitleCLabel;
    private JLabel PasswordCLabel;
    private JLabel LoginPageLabel;
    private JButton LoginPageButton;
    private JLabel LogoLabel;
    private JTextField UsernameForm;
    private JLabel UsernameLabel;
    private JPasswordField PasswordField;
    private JPanel CRAButtonWrap;
    private JButton LoginButton;
    private JPasswordField passwordField1;
    private JLabel RePassLabel;

    public signup_page() {
        setContentPane(MainCPanel);
        setTitle("VetConnect Account Creation");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1920,1080);
        setLocationRelativeTo(null);

        LoginPageButton.addActionListener(e -> {
            new login_page();
            setVisible(false);
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new signup_page());
    }
    private void createUIComponents() {
        TitleCLabel = new JLabel("Welcome to VetConnect"); // set whatever text you want
        Font T = TitleCLabel.getFont();
        TitleCLabel.setFont(T.deriveFont(45f));
        TitleCLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        UsernameLabel = new JLabel("Username:"); // set whatever text you want
        Font U = UsernameLabel.getFont();
        UsernameLabel.setFont(U.deriveFont(25f));
        UsernameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        PasswordCLabel = new JLabel("Password:"); // set whatever text you want
        Font P = PasswordCLabel.getFont();
        PasswordCLabel.setFont(P.deriveFont(25f));
        PasswordCLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        RePassLabel = new JLabel("Re-Enter Password:");
        Font RP = RePassLabel.getFont();
        RePassLabel.setFont(P.deriveFont(25f));
        RePassLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        LoginPageLabel = new JLabel("Already have an account?"); // set whatever text you want
        Font C = LoginPageLabel.getFont();
        LoginPageLabel.setFont(C.deriveFont(15f));
        LoginPageLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        CRAButtonWrap = new JPanel();
        CRAButtonWrap.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // image label
        LogoLabel = new JLabel();
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/temp_logo.jpg"));
        // image scaling:
        Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        LogoLabel.setIcon(new ImageIcon(scaled));
    }
}
