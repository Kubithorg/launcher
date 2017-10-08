package fr.ironcraft.kubithon.launcher;

import fr.theshark34.swinger.event.SwingerEvent;
import fr.theshark34.swinger.event.SwingerEventListener;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import jiconfont.icons.FontAwesome;

public class NewLauncherPanel extends JPanel implements SwingerEventListener
{
    public static final Color BACKGROUND = new Color(50, 50, 50);
    public static final Color FOREGROUND = new Color(42, 42, 42);

    public static final Font FONT = new Font("Arial", Font.PLAIN, 20);

    private boolean premium = false;

    private LauncherFrame parentFrame;

    private KubithonButton quitButton;
    private KubithonButton hideButton;

    // General
    private KubithonButton launchButton;
    private KubithonButton switchButton;

    // Crack
    private JTextField usernameField;

    // Premium
    private AntialiasingLabel infosLabel;

    public NewLauncherPanel(LauncherFrame parentFrame)
    {
        this.setLayout(null);
        this.setBackground(BACKGROUND);

        this.parentFrame = parentFrame;

        quitButton = new KubithonButton(FontAwesome.TIMES);
        quitButton.addEventListener(this);
        quitButton.setBounds(LauncherFrame.WIDTH - 30, 0, 30, 30);
        this.add(quitButton);

        hideButton = new KubithonButton(FontAwesome.MINUS);
        hideButton.addEventListener(this);
        hideButton.setBounds(LauncherFrame.WIDTH - 60, 0, 30, 30);
        this.add(hideButton);

        launchButton = new KubithonButton("Installer");
        launchButton.addEventListener(this);
        launchButton.setBounds(0, LauncherFrame.HEIGHT - 35, LauncherFrame.WIDTH - 35, 35);
        this.add(launchButton);

        switchButton = new KubithonButton(FontAwesome.EXCHANGE);
        switchButton.setToolTipText("Non-premium");
        switchButton.addEventListener(this);
        switchButton.setBounds(LauncherFrame.WIDTH - 35, LauncherFrame.HEIGHT - 35, 35, 35);
        this.add(switchButton);

        infosLabel = new AntialiasingLabel();
        infosLabel.setForeground(Color.WHITE);
        infosLabel.setFont(infosLabel.getFont().deriveFont(18f));
        infosLabel.setText("<html><center>Kubithon v1.0.0<br>Status : <font color='#22ee22'>Prêt</font></center></html>");
        infosLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // infosLabel.setVisible(false);
        infosLabel.setBounds(35, LauncherFrame.HEIGHT - 150, LauncherFrame.WIDTH - 70, 75);
        this.add(infosLabel);
    }


    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        g.setColor(FOREGROUND);
        g.fillRect(0, 0, this.getWidth(), 30);
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

        }
        else if (e.getSource() == launchButton)
        {

        }
    }
}
