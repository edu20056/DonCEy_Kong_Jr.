#include "client.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/select.h>
#include <termios.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include "raylib.h"

// Variables de conexión
#define BUFFER_SIZE 1024
#define SERVER_IP "127.0.0.1"
#define SERVER_PORT 5000

int extraer_num(const char *s) {
    int n = 0;
    sscanf(s, "%d", &n);
    return n;
}

void procesarJSON(const char *json) {
    // ===========================
    //   JUGADOR
    // ===========================
    char *jug = strstr(json, "\"jugador\"");
    if (jug) {
        char *xpos = strstr(jug, "\"x\"");
        char *ypos = strstr(jug, "\"y\"");

        if (xpos && ypos) {
            int x = extraer_num(xpos + 4);
            int y = extraer_num(ypos + 4);

            printf("Jugador: x=%d, y=%d\n", x, y);
        }
    }

    // ===========================
    //   ENTIDADES
    // ===========================
    printf("Entidades:\n");

    char *ent = strstr(json, "\"entidades\"");
    if (ent) {
        char *p = ent;
        while (1) {
            char *xpos = strstr(p, "\"x\"");
            char *ypos = strstr(p, "\"y\"");

            if (!xpos || !ypos) break;

            int x = extraer_num(xpos + 4);
            int y = extraer_num(ypos + 4);

            printf("  Entidad: x=%d, y=%d\n", x, y);

            p = ypos + 4;
        }
    }

    // ===========================
    //   FRUTAS
    // ===========================
    printf("Frutas:\n");

    char *fru = strstr(json, "\"frutas\"");
    if (fru) {
        char *p = fru;
        while (1) {
            char *xpos = strstr(p, "\"x\"");
            char *ypos = strstr(p, "\"y\"");

            if (!xpos || !ypos) break;

            int x = extraer_num(xpos + 4);
            int y = extraer_num(ypos + 4);

            printf("  Fruta: x=%d, y=%d\n", x, y);

            p = ypos + 4;
        }
    }
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

// =====================================================
//   ESTA ES LA FUNCIÓN PRINCIPAL DEL CLIENTE
//   (REEMPLAZA A TU main ORIGINAL)
// =====================================================
void *run_client(void *arg) {

    int sock;
    struct sockaddr_in server_addr;
    char buffer[BUFFER_SIZE];
    char tipo[20];
    char nombre[50];
    int esJugador = 0;

    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        perror("Error al crear socket");
        exit(EXIT_FAILURE);
    }

    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons((uint16_t)SERVER_PORT);

    if (inet_pton(AF_INET, SERVER_IP, &server_addr.sin_addr) <= 0) {
        perror("Dirección inválida");
        close(sock);
        exit(EXIT_FAILURE);
    }

    if (connect(sock, (struct sockaddr *)&server_addr, sizeof(server_addr)) < 0) {
        perror("Error al conectar con el servidor");
        close(sock);
        exit(EXIT_FAILURE);
    }

    printf("Conectado al servidor Java.\n");

    int bienvenida_completa = 0;
    while (!bienvenida_completa) {
        ssize_t bytes = recv(sock, buffer, sizeof(buffer) - 1, 0);
        if (bytes <= 0) {
            printf("Error al recibir bienvenida del servidor\n");
            close(sock);
            return NULL;
        }
        buffer[bytes] = '\0';

        printf("%s", buffer);

        if (strstr(buffer, "Conexión Exitosa.") != NULL) {
            bienvenida_completa = 1;
        }
    }

    printf("\nSeleccione tipo de cliente:\n");
    printf("1. Jugador\n");
    printf("2. Espectador\n");
    printf("> ");

    if (fgets(tipo, sizeof(tipo), stdin) == NULL) {
        close(sock);
        return NULL;
    }
    trim_newline(tipo);

    char sendbuf[BUFFER_SIZE];
    if (strcmp(tipo, "1") == 0) {
        esJugador = 1;
        printf("Nombre del jugador: ");
        if (fgets(nombre, sizeof(nombre), stdin) == NULL) {
            close(sock);
            return NULL;
        }
        trim_newline(nombre);
        snprintf(sendbuf, sizeof(sendbuf), "PLAYER %s\n", nombre);
    }
    else if (strcmp(tipo, "2") == 0) {
        esJugador = 0;
        snprintf(sendbuf, sizeof(sendbuf), "SPECTATOR\n");
    }
    else {
        printf("Opción inválida.\n");
        close(sock);
        return NULL;
    }

    printf("Enviando tipo de cliente al servidor: %s", sendbuf);
    send(sock, sendbuf, strlen(sendbuf), 0);

    if (esJugador) {

        printf("Esperando confirmación del servidor...\n");

        while (1) {
            ssize_t bytes = recv(sock, buffer, sizeof(buffer) - 1, 0);
            if (bytes <= 0) {
                printf("Error del servidor o conexión cerrada\n");
                close(sock);
                return NULL;
            }
            buffer[bytes] = '\0';

            printf("Servidor: %s", buffer);

            if (strstr(buffer, "ERROR") != NULL) {
                printf("Error al registrarse. Saliendo...\n");
                close(sock);
                return NULL;
            }

            if (strstr(buffer, "OK:") != NULL || strstr(buffer, "Puede comenzar") != NULL) {
                break;
            }
        }

        printf("Use teclas W A S D para moverse. ('q' para salir)\n> ");
        fflush(stdout);

        set_input_mode();

        fd_set readfds;

        while (1) {
            FD_ZERO(&readfds);
            FD_SET(sock, &readfds);
            FD_SET(STDIN_FILENO, &readfds);

            int max_fd = (sock > STDIN_FILENO) ? sock : STDIN_FILENO;
            int activity = select(max_fd + 1, &readfds, NULL, NULL, NULL);

            if (activity < 0) {
                perror("Error en select");
                break;
            }

            if (FD_ISSET(sock, &readfds)) {
                ssize_t bytes = recv(sock, buffer, sizeof(buffer) - 1, 0);
                if (bytes <= 0) {
                    printf("\nServidor desconectado.\n");
                    break;
                }
                buffer[bytes] = '\0';

                procesarJSON(buffer);
                printf("\n%s\n> ", buffer);
                fflush(stdout);
            }

            if (FD_ISSET(STDIN_FILENO, &readfds)) {
                char tecla;
                ssize_t bytes = read(STDIN_FILENO, &tecla, 1);
                if (bytes <= 0) continue;

                char msg[4] = {0};

                switch (tecla) {
                    case 'w': case 'W': strcpy(msg, "1"); break;
                    case 'd': case 'D': strcpy(msg, "2"); break;
                    case 's': case 'S': strcpy(msg, "3"); break;
                    case 'a': case 'A': strcpy(msg, "4"); break;
                    case 'q': case 'Q':
                        printf("Saliendo...\n");
                        reset_input_mode();
                        close(sock);
                        return 0;
                    default:
                        continue;
                }

                strcat(msg, "\n");
                send(sock, msg, strlen(msg), 0);
            }
        }

        reset_input_mode();
    }
    else {
        // ===========================
        //   MODO ESPECTADOR
        // ===========================
        printf("Modo espectador.\n");

        char total_buffer[BUFFER_SIZE * 4] = {0};
        int lista_completa = 0;

        while (!lista_completa) {
            ssize_t bytes = recv(sock, buffer, sizeof(buffer) - 1, 0);
            if (bytes <= 0) {
                printf("Error: No se pudo recibir lista\n");
                close(sock);
                return NULL;
            }
            buffer[bytes] = '\0';
            strcat(total_buffer, buffer);

            if (strstr(total_buffer, "END_PLAYERS_LIST") != NULL) {
                lista_completa = 1;
            }

            usleep(100000);
        }

        char *start = strstr(total_buffer, "Jugadores disponibles:");
        char *end = strstr(total_buffer, "END_PLAYERS_LIST");

        if (start && end) {
            *end = '\0';
            printf("%s\n", start);
        } else {
            printf("%s\n", total_buffer);
        }

        printf("Ingrese el número de jugador a espectear: ");
        if (fgets(nombre, sizeof(nombre), stdin) == NULL) {
            close(sock);
            return NULL;
        }
        trim_newline(nombre);

        char nombre_con_nl[BUFFER_SIZE * 2];
        snprintf(nombre_con_nl, sizeof(nombre_con_nl), "%s\n", nombre);
        send(sock, nombre_con_nl, strlen(nombre_con_nl), 0);

        printf("Esperando confirmación...\n");

        while (1) {
            ssize_t bytes = recv(sock, buffer, sizeof(buffer) - 1, 0);
            if (bytes <= 0) {
                printf("Conexión terminada.\n");
                break;
            }
            buffer[bytes] = '\0';

            if (strstr(buffer, "ERROR") != NULL) {
                printf("Error: %s\n", buffer);
                break;
            }

            if (strstr(buffer, "OK:") != NULL || strstr(buffer, "Conectado") != NULL) {
                printf("Servidor: %s", buffer);
                printf("\n=== Modo Espectador Activado ===\n");
                continue;
            }

            procesarJSON(buffer);
            printf("%s", buffer);
        }
    }

    close(sock);
    printf("Conexión cerrada.\n");
    return NULL;
}
