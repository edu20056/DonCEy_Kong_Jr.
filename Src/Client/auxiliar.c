#include "auxiliar.h"
#include <unistd.h>
#include <termios.h>
#include "graphics.h"
#include <threads.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include "raylib.h"

int extraer_num(const char *s) {
    int n = 0;
    sscanf(s, "%d", &n);
    return n;
}


void trim_newline(char *str) {
    size_t len = strlen(str);
    while (len > 0 && (str[len - 1] == '\n' || str[len - 1] == '\r')) {
        str[--len] = '\0';
    }
}

void set_input_mode(void) {
    struct termios tattr;

    tcgetattr(STDIN_FILENO, &tattr);
    tattr.c_lflag &= ~(ICANON | ECHO);
    tattr.c_cc[VMIN] = 1;
    tattr.c_cc[VTIME] = 0;
    tcsetattr(STDIN_FILENO, TCSANOW, &tattr);
}

void reset_input_mode(void) {
    struct termios tattr;
    tcgetattr(STDIN_FILENO, &tattr);
    tattr.c_lflag |= ICANON | ECHO;
    tcsetattr(STDIN_FILENO, TCSANOW, &tattr);
}
