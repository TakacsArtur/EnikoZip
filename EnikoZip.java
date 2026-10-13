import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.zip.*;
import org.tukaani.xz.XZInputStream; // needs xz-1.9.jar (or newer) on the classpath

public class EnikoZip {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("EnikoZip");
            JLabel status = new JLabel("Pick a .zip or .xz file", SwingConstants.CENTER);
            JButton button = new JButton("Choose file...");

            button.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
                File file = chooser.getSelectedFile();
                try {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".zip")) unzip(file);
                    else if (name.endsWith(".xz")) unxz(file);
                    else throw new IOException("Not a .zip or .xz file");
                    status.setText("Done!");
                } catch (Exception ex) {
                    status.setText("Error: " + ex.getMessage());
                }
            });

            frame.setLayout(new BorderLayout(10, 10));
            frame.add(status, BorderLayout.CENTER);
            frame.add(button, BorderLayout.SOUTH);
            frame.setSize(350, 120);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Extracts a .zip into a folder next to it, named after the zip
    static void unzip(File zip) throws IOException {
        String base = zip.getName().substring(0, zip.getName().length() - 4);
        Path outDir = new File(zip.getParentFile(), base).toPath().toAbsolutePath().normalize();
        Files.createDirectories(outDir);

        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                Path target = outDir.resolve(entry.getName()).normalize();
                if (!target.startsWith(outDir)) throw new IOException("Bad zip entry: " + entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    // Decompresses a .xz file into the same folder (foo.txt.xz -> foo.txt)
    static void unxz(File xz) throws IOException {
        String base = xz.getName().substring(0, xz.getName().length() - 3);
        Path target = new File(xz.getParentFile(), base).toPath();

        try (InputStream in = new XZInputStream(new BufferedInputStream(new FileInputStream(xz)))) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
