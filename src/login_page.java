import javax.swing.*;
import java.awt.*;
import javax.swing.BorderFactory;


public class login_page extends JFrame {
    private JButton CreateACCButton;
    private JLabel TitleLabel;
    private JPanel MainPanel;
    private JLabel SubTitle;
    private JLabel UsernameLabel;
    private JLabel PasswordLabel;
    private JLabel CreateACCLabel;
    private JLabel LogoLabel;
    private JPanel LoginPanel;
    private JPasswordField PasswordField;
    private JTextField UsernameForm;
    private JButton forgotPasswordButton;
    private JButton LoginButton;
    private JPanel LoginButtonWrap;


    public login_page() {
        setContentPane(MainPanel);
        setTitle("VetConnect Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1920,1080);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new login_page();
    }
    private void createUIComponents() {
        TitleLabel = new JLabel("Welcome to VetConnect"); // set whatever text you want
        Font T = TitleLabel.getFont();
        TitleLabel.setFont(T.deriveFont(45f));
        TitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        SubTitle = new JLabel("Please enter your login information below"); // set whatever text you want
        Font S = SubTitle.getFont();
        SubTitle.setFont(S.deriveFont(25f));
        SubTitle.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        UsernameLabel = new JLabel("Username:"); // set whatever text you want
        Font U = UsernameLabel.getFont();
        UsernameLabel.setFont(U.deriveFont(25f));
        UsernameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        PasswordLabel = new JLabel("Password:"); // set whatever text you want
        Font P = PasswordLabel.getFont();
        PasswordLabel.setFont(P.deriveFont(25f));
        PasswordLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        CreateACCLabel = new JLabel("Don't have an account, create one here"); // set whatever text you want
        Font C = CreateACCLabel.getFont();
        CreateACCLabel.setFont(C.deriveFont(15f));
        CreateACCLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        LoginButtonWrap = new JPanel();
        LoginButtonWrap.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // image label
        LogoLabel = new JLabel();
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/temp_logo.jpg"));
        // image scaling:
        Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        LogoLabel.setIcon(new ImageIcon(scaled));
    }
}



