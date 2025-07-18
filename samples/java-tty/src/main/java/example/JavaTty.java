package example;

import com.jakewharton.mosaic.tty.Tty;
import java.io.IOException;
import java.io.PrintStream;

public final class JavaTty {
	public static void main(String[] args) throws InterruptedException, IOException {
		try (Tty tty = Tty.tryBind()) {
			if (tty == null) {
				System.err.println("No TTY found!");
				System.exit(1);
			}

			tty.enableRawMode();
			System.setOut(new PrintStream(tty.asOutputStream()));

			counter();
		}
	}

	private static void counter() throws InterruptedException {
		System.out.print("The count is: 0\r\n");
		for (int i = 1; i <= 20; i++) {
			Thread.sleep(250);

			System.out.print("\u001b[A"); // Go up a line
			System.out.print("The count is: ");
			System.out.print(i);
			System.out.print("\u001b[K"); // Clear rest of line
			System.out.print("\r\n");
		}
	}

	private JavaTty() {}
}
