import java.io.OutputStream;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Sends normal program output into a Swing text area.
 * This lets the GUI reuse the existing manager messages without changing them.
 */
public class GuiOutput extends OutputStream {

    private final JTextArea area;

    public GuiOutput(JTextArea area) {
        this.area = area;
    }

    @Override
    public void write(int b) {
        append(String.valueOf((char) b));
    }

    @Override
    public void write(byte[] data, int offset, int length) {
        append(new String(data, offset, length));
    }

    private void append(String text) {
        if (SwingUtilities.isEventDispatchThread()) {
            appendNow(text);
        } else {
            SwingUtilities.invokeLater(() -> appendNow(text));
        }
    }

    private void appendNow(String text) {
        area.append(text);
        area.setCaretPosition(area.getDocument().getLength());
    }
}
