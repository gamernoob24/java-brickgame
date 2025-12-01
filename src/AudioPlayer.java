import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class AudioPlayer {

    private Clip musicClip;

    public void playMusic(String fileName) {
        try {
            if (musicClip != null && musicClip.isOpen()) {
                musicClip.stop();
                musicClip.close();
            }
            URL url = getClass().getResource(fileName);
            if (url == null) {
                System.err.println("Couldn't find file: " + fileName);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioIn);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
        }
    }

    public void playSound(String fileName) {
        try {
            URL url = getClass().getResource(fileName);
            if (url == null) {
                System.err.println("Couldn't find file: " + fileName);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    event.getLine().close();
                }
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
