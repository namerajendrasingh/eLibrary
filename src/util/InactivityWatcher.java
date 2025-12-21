package util;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class InactivityWatcher {

    private static final long INACTIVITY_DELAY_MS = 10 * 60_000; // 10 minute
    private static Timer timer;

    public static void install(Runnable onTimeout) {
        // Start Swing timer
        timer = new Timer((int) INACTIVITY_DELAY_MS, (ActionEvent e) -> onTimeout.run());
        timer.setRepeats(false);
        timer.start();

        // Listen to all AWT events and reset timer when user does something
        AWTEventListener listener = (AWTEvent event) -> resetTimer(onTimeout);
        long mask = AWTEvent.KEY_EVENT_MASK
                  | AWTEvent.MOUSE_EVENT_MASK
                  | AWTEvent.MOUSE_MOTION_EVENT_MASK
                  | AWTEvent.MOUSE_WHEEL_EVENT_MASK;

        Toolkit.getDefaultToolkit().addAWTEventListener(listener, mask);
    }

    private static void resetTimer(Runnable onTimeout) {
        if (timer == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            timer.restart();
        } else {
            SwingUtilities.invokeLater(timer::restart);
        }
    }
}
