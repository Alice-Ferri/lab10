package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final String MIN = "minimum:";
    private static final String MAX = "maximum:";
    private static final String ATTEMPTS = "attempts:";
    private static final String FILE_NAME = "config.yml";

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) throws IOException {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }

        int min = 0;
        int max = 0;
        int attempts = 0;

        try (BufferedReader str = new BufferedReader(
                new InputStreamReader(
                    ClassLoader.getSystemResourceAsStream(FILE_NAME), StandardCharsets.UTF_8)
            )) {
            try {
                String newLine = str.readLine();
                while (newLine != null) {
                    final StringTokenizer token = new StringTokenizer(newLine);
                        switch (token.nextToken()) {
                            case MIN:
                                min = Integer.parseInt(token.nextToken().trim());
                                break;
                            case MAX:
                                max = Integer.parseInt(token.nextToken().trim());
                                break;
                            case ATTEMPTS:
                                attempts = Integer.parseInt(token.nextToken().trim());
                                break;
                            default:
                                throw new AssertionError();
                        }
                    newLine = str.readLine();
                }
        } catch (IOException e) {
            System.out.println(e.getCause()); //NOPMD
        }
        this.model = new DrawNumberImpl(min, max, attempts);
    }
}

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    /**
     * @param args
     *            ignored
     * @throws IOException 
     */
    public static void main(final String... args) throws IOException {
        new DrawNumberApp(new DrawNumberViewImpl(), new PrintStreamView(System.out));
    }

}
