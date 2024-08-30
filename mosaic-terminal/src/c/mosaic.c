#include "mosaic.h"

#if defined(__APPLE__) || defined(__linux__)

#include <stdio.h>
#include <unistd.h>
#include <termios.h>

struct termios saved;

bool tty_save() {
	return tcgetattr(STDIN_FILENO, &saved) == 0;
}

bool tty_restore() {
	return tcsetattr(STDIN_FILENO, TCSAFLUSH, &saved) == 0;
}

bool tty_setRawMode() {
	struct termios current;
	if (tcgetattr(STDIN_FILENO, &current) != 0) {
		return false;
	}

	// Flags as defined by "Raw mode" section of https://linux.die.net/man/3/termios
	current.c_iflag &= ~(BRKINT | ICRNL | IGNBRK | IGNCR | INLCR | ISTRIP | IXON | PARMRK);
	current.c_oflag &= ~(OPOST);
	// Setting ECHONL should be useless here, but it is what is documented for cfmakeraw.
	current.c_lflag &= ~(ECHO | ECHONL | ICANON | IEXTEN | ISIG);
	current.c_cflag &= ~(CSIZE | PARENB);
	current.c_cflag |= (CS8);

	current.c_cc[VMIN] = 1;
	current.c_cc[VTIME] = 0;

	return tcsetattr(STDIN_FILENO, TCSAFLUSH, &current) == 0;
}

#else

#include <Windows.h>

DWORD saved_input_mode;
DWORD saved_output_mode;
UINT saved_output_code_page;

bool tty_save() {
	HANDLE stdin = GetStdHandle(STD_INPUT_HANDLE);
	HANDLE stdout = GetStdHandle(STD_OUTPUT_HANDLE);

	saved_output_code_page = GetConsoleOutputCP();
	if (saved_output_code_page == 0) {
		return false;
	}
	if (GetConsoleMode(stdin, &saved_input_mode) == 0) {
		return false;
	}
	if (GetConsoleMode(stdout, &saved_output_mode) == 0) {
		return false;
	}
	return true;
}

bool tty_restore() {
	HANDLE stdin = GetStdHandle(STD_INPUT_HANDLE);
	HANDLE stdout = GetStdHandle(STD_OUTPUT_HANDLE);

	if (SetConsoleMode(stdin, saved_input_mode) == 0) {
		return false;
	}
	if (SetConsoleMode(stdout, saved_output_mode) == 0) {
		return false;
	}
	if (SetConsoleOutputCP(saved_output_code_page) == 0) {
		return false;
	}
	return true;
}

bool tty_setRawMode() {
	HANDLE stdin = GetStdHandle(STD_INPUT_HANDLE);
	HANDLE stdout = GetStdHandle(STD_OUTPUT_HANDLE);

	if (SetConsoleMode(stdin, ENABLE_WINDOW_INPUT | ENABLE_MOUSE_INPUT | ENABLE_EXTENDED_FLAGS) == 0) {
		return false;
	}
	if (SetConsoleMode(stdout, ENABLE_PROCESSED_OUTPUT | ENABLE_VIRTUAL_TERMINAL_PROCESSING | DISABLE_NEWLINE_AUTO_RETURN | ENABLE_LVB_GRID_WORLDWIDE) == 0) {
		return false;
	}
	if (SetConsoleOutputCP(65001 /* UTF-8 */) == 0) {
		return false;
	}
	return true;
}

#endif
