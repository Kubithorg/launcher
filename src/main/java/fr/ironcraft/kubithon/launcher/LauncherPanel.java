package fr.ironcraft.kubithon.launcher;

import fr.theshark34.openlauncherlib.util.Saver;
import fr.theshark34.swinger.Swinger;
import fr.theshark34.swinger.colored.SColoredBar;
import fr.theshark34.swinger.event.SwingerEvent;
import fr.theshark34.swinger.event.SwingerEventListener;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import jiconfont.icons.FontAwesome;

public class LauncherPanel extends JPanel implements SwingerEventListener
{
    public static final Color BACKGROUND = new Color(50, 50, 50);
    public static final Color FOREGROUND = new Color(42, 42, 42);

    public static final String GREEN = "#22ee22";
    public static final String BLUE = "#0099ec";
    public static final String YELLOW = "#eeee22";
    public static final String RED = "#ee2222";

    public static final Font FONT = new Font("Arial", Font.PLAIN, 20);
    public static final BufferedImage LOGO = Swinger.getResource("logo.png");
    public static final BufferedImage FLECHE = Swinger.getResource("ptite_fleche.png");

    private Saver saver = new Saver(new File(Launcher.KUBITHON_DIR, "launcher.properties"));

    private boolean premium = false;
    private boolean first = saver.get("first", "true").equalsIgnoreCase("true");
    {
        saver.set("first", "false");
    }

    private LauncherFrame parentFrame;

    private KubithonButton hideButton;
    private KubithonButton quitButton;

    // General
    private KubithonButton launchButton;
    private KubithonButton switchButton;
    private SColoredBar progressBar;

    // Crack
    private KubithonTextField usernameField;
    private KubithonPasswordField passwordField;
    private AntialiasingLabel registerLabel;

    // Premium
    private AntialiasingLabel infosLabel;

    public LauncherPanel(LauncherFrame parentFrame)
    {
        this.setLayout(null);
        this.setBackground(BACKGROUND);

        this.parentFrame = parentFrame;

        hideButton = new KubithonButton(FontAwesome.MINUS);
        hideButton.addEventListener(this);
        hideButton.setBounds(LauncherFrame.WIDTH - 60, 0, 30, 30);
        this.add(hideButton);

        quitButton = new KubithonButton(FontAwesome.TIMES);
        quitButton.addEventListener(this);
        quitButton.setBounds(LauncherFrame.WIDTH - 30, 0, 30, 30);
        this.add(quitButton);

        usernameField = new KubithonTextField(saver.get("username", ""), "Nom d'utilisateur");
        usernameField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE));
        usernameField.setHorizontalAlignment(SwingConstants.CENTER);
        usernameField.setBackground(Swinger.TRANSPARENT);
        usernameField.setOpaque(false);
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(usernameField.getForeground());
        usernameField.setCaretPosition(usernameField.getText().length());
        usernameField.setFont(usernameField.getFont().deriveFont(16f));
        usernameField.setBounds(125, 220, 200, 30);
        this.add(usernameField);

        passwordField = new KubithonPasswordField("Mot de passe");
        passwordField.setBorder(usernameField.getBorder());
        passwordField.setHorizontalAlignment(SwingConstants.CENTER);
        passwordField.setBackground(Swinger.TRANSPARENT);
        passwordField.setOpaque(false);
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(passwordField.getForeground());
        passwordField.setFont(passwordField.getFont());
        passwordField.setBounds(125, 257, 200, 30);
        this.add(passwordField);

        registerLabel = new AntialiasingLabel();
        registerLabel.setText("<html><u>S'inscrire</u></html>");
        registerLabel.setForeground(Color.WHITE);
        registerLabel.setFont(registerLabel.getFont().deriveFont(12f));
        registerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        registerLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent mouseEvent)
            {
                if(Desktop.isDesktopSupported())
                {
                    try
                    {
                        Desktop.getDesktop().browse(new URI("https://kubithon.org/register"));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    Runtime runtime = Runtime.getRuntime();
                    try
                    {
                        runtime.exec("xdg-open https://kubithon.org/register");
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
        registerLabel.setBounds(0, 295, LauncherFrame.WIDTH, 20);
        this.add(registerLabel);

        progressBar = new SColoredBar(Swinger.getTransparentWhite(75), Swinger.getTransparentWhite(175));
        progressBar.setVisible(false);
        progressBar.setBounds(0, LauncherFrame.HEIGHT - 37, LauncherFrame.WIDTH, 2);
        this.add(progressBar);

        infosLabel = new AntialiasingLabel();
        infosLabel.setForeground(Color.WHITE);
        infosLabel.setFont(infosLabel.getFont().deriveFont(18f));
        infosLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infosLabel.setBounds(35, LauncherFrame.HEIGHT - 170, LauncherFrame.WIDTH - 70, 75);
        this.add(infosLabel);

        launchButton = new KubithonButton("Installer");
        launchButton.addEventListener(this);
        launchButton.setBounds(0, LauncherFrame.HEIGHT - 35, LauncherFrame.WIDTH - 35, 35);
        this.add(launchButton);

        switchButton = new KubithonButton(FontAwesome.EXCHANGE);
        switchButton.addEventListener(this);
        switchButton.setBounds(LauncherFrame.WIDTH - 35, LauncherFrame.HEIGHT - 35, 35, 35);
        this.add(switchButton);

        setPremium(saver.get("premium", "true").equals("true"), true);

        passwordField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent keyEvent)
            {
                if (launchButton.isEnabled() && keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    onEvent(new SwingerEvent(launchButton, SwingerEvent.BUTTON_CLICKED_EVENT));
                }
            }
        });
    }

    protected void setPremium(boolean premium, boolean init)
    {
        if (!init)
        {
            first = false;
        }

        Rectangle previous = infosLabel.getBounds();

        if (this.premium = premium)
        {
            infosLabel.setBounds(previous.x, LauncherFrame.HEIGHT - 170, previous.width, previous.height);
        }
        else
        {
            infosLabel.setBounds(previous.x, LauncherFrame.HEIGHT - 130, previous.width, previous.height);
        }

        usernameField.setVisible(!premium);
        passwordField.setVisible(!premium);
        registerLabel.setVisible(!premium);

        if (usernameField.getText().trim().isEmpty())
        {
            usernameField.requestFocus();
        }
        else
        {
            passwordField.requestFocus();
        }

        launchButton.setText(premium ? "Installer" : "Lancer");
        switchButton.setToolTipText("Passer " + (premium ? "Non-Premium" : "Premium"));

        setStatus("Prêt", GREEN);
        repaint();
    }

    protected void setStatus(String status, String statusColor)
    {
        String modeColor = premium ? GREEN : YELLOW;
        String mode = premium ? "Premium" : "Non-Premium";

        infosLabel.setText("<html><center>Kubithon " + KubithonInstaller.VERSION + "<br>Mode : <font color='" + modeColor + "'>" + mode + "</font><br>Status : <font color='" + statusColor + "'>" + status + "</font></center></html>");
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Swinger.activateAntialias(g);

        g.setColor(FOREGROUND);
        g.fillRect(0, 0, this.getWidth(), 30);

        int width = 137; // 1097 / 8
        int height = 158; // 1267 / 8

        g.drawImage(LOGO, this.getWidth() / 2 - width / 2, premium ? 75 : 47, width, height, this);

        if (first)
        {
            g.drawImage(FLECHE, this.getWidth() - FLECHE.getWidth() - 12, this.getHeight() - FLECHE.getHeight() - 35, FLECHE.getWidth(), FLECHE.getHeight(), this);
        }
    }

    @Override
    public void onEvent(SwingerEvent e)
    {
        if (e.getSource() == quitButton)
        {
            System.exit(0);
        }
        else if (e.getSource() == hideButton)
        {
            parentFrame.setState(JFrame.ICONIFIED);
        }
        else if (e.getSource() == switchButton)
        {
            this.setPremium(!this.premium, false);
        }
        else if (e.getSource() == launchButton)
        {
            launch();
        }
    }

    protected void launch()
    {
        disable(true);

        saver.set("username", usernameField.getText());
        saver.set("premium", String.valueOf(this.premium));

        final Launcher launcher = new Launcher(this);

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                if (premium)
                {
                    launcher.premium();
                }
                else
                {
                    launcher.nonPremium(usernameField.getText(), new String(passwordField.getPassword()), parentFrame);
                    progressBar.setVisible(false);
                }

                disable(false);
            }
        };
        t.start();
    }

    protected void disable(boolean disable)
    {
        usernameField.setEnabled(!disable);
        passwordField.setEnabled(!disable);
        launchButton.setEnabled(!disable);
        switchButton.setEnabled(!disable);

        if (!disable && !premium)
        {
            passwordField.requestFocus();
        }
    }

    public SColoredBar getProgressBar()
    {
        return progressBar;
    }
}
