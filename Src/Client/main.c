#include "graphics.h"
#include <threads.h>
#include <string.h>
#include <unistd.h>
#include <sys/select.h>
#include <termios.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include "auxiliar.h"
#include <raylib.h>
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <stdatomic.h>
#include <sys/time.h>

atomic_int terminar_es = 0;


// Variables de conexión
#define BUFFER_SIZE 1024
#define SERVER_IP "127.0.0.1"
#define SERVER_PORT 5000

// Estructuras, partes gráficas
typedef struct {
    int x;
    int y;
    char type[20]; 
    bool view;
} Entidad;

typedef struct {
    int x;
    int y;
    char type[20];   // "BANANA", "STRAWBERRY", "NARANJA"
    int points;
} Fruta;

typedef struct {
    int x;
    int y;
    int climbing;
    int right;
    int points;
    char PlayerName[20]; 
    int espectadores;
    int dead;
    int vidas;
} Jugador;

// Hilo de cliente
thrd_t thread_client;

// Instancia global del jugador
Jugador jugador = {10, 10, 0, 0, 0, "", 0, 0, 0};

// Entidades dinámicas
#define MAX_ENTIDADES 100
Entidad entidades[MAX_ENTIDADES];
int numEntidades = 0;

// Frutas dinámicas
#define MAX_FRUTAS 100
Fruta frutas[MAX_FRUTAS];
int numFrutas = 0;

// ======================================================
// ======================= CÓDIGO =======================
// ======================================================

void extraer_string(const char *p, char *dest, int max_len) {
    char *inicio = strchr(p, '\"');
    if (!inicio) return;
    inicio++; // saltamos la comilla

    char *fin = strchr(inicio, '\"');
    if (!fin) return;

    int len = fin - inicio;
    if (len >= max_len) len = max_len - 1;

    strncpy(dest, inicio, len);
    dest[len] = '\0';
}

void terminarEspectador() {
    terminar_es = 1;
}

void procesarJSON(const char *json) {

    // ===== JUGADOR =====
    char *jug = strstr(json, "\"jugador\"");
    if (jug) {
        char *inicio = strchr(jug, '{');
        if (!inicio) return;

        char *fin = inicio;
        int llaves = 0;
        do {
            if (*fin == '{') llaves++;
            if (*fin == '}') llaves--;
            fin++;
        } while (llaves > 0 && *fin);

        char *xpos = strstr(inicio, "\"x\"");
        char *ypos = strstr(inicio, "\"y\"");
        char *pts  = strstr(inicio, "\"puntos\"");
        char *climb = strstr(inicio, "\"climbing\"");
        char *right = strstr(inicio, "\"right\"");
        char *name = strstr(jug, "\"name\"");
        char *spect = strstr(inicio, "\"spect\""); 
        char *dead = strstr(inicio, "\"dead\""); 
        char *lives = strstr(inicio, "\"lives\""); 

        if (xpos && xpos < fin) jugador.x = extraer_num(xpos + 4) * 20;
        if (ypos && ypos < fin) jugador.y = extraer_num(ypos + 4) * 20;
        if (pts  && pts  < fin) jugador.points = extraer_num(pts + 9);
        if (climb != NULL) {
            char *colon = strchr(climb, ':');
            if (colon != NULL) {
                char *val = colon + 1;
                while (*val == ' ' || *val == '\t') val++;
                jugador.climbing = (*val == 't') ? 1 : 0;
            }
        }

        if (dead != NULL) {
            char *colon = strchr(dead, ':');
            if (colon != NULL) {
                char *val = colon + 1;
                while (*val == ' ' || *val == '\t') val++;
                jugador.dead = (*val == 't') ? 1 : 0;
                printf("[DEBUG] valor dead = %c, jugador.dead = %d\n", *val, jugador.dead);
            }
        }
        if (right != NULL) {
            // saltar hasta ':'
            char *colon = strchr(right, ':');
            if (colon != NULL) {
                // saltar espacios y comparar la primera letra del valor
                char *val = colon + 1;
                while (*val == ' ' || *val == '\t') val++;
                jugador.right = (*val == 't') ? 1 : 0;
            }
        }

        // ===== name =====
        if (name && name < fin) {
            char *colon = strchr(name, ':');
            if (colon) {
                char *val = colon + 1;

                // Saltar espacios
                while (*val == ' ' || *val == '\t') val++;

                // Debe iniciar con comilla "
                if (*val == '"') {
                    val++; // saltar primera comilla

                    int i = 0;
                    while (*val && *val != '"' && i < sizeof(jugador.PlayerName) - 1) {
                        jugador.PlayerName[i++] = *val++;
                    }

                    jugador.PlayerName[i] = '\0';
                }
            }
        }

        // ===== spectators =====
        if (spect && spect < fin) {
            // spect: <numero>
            char *col = strchr(spect, ':');
            if (col) {
                jugador.espectadores = extraer_num(col + 1);
                printf("[DEBUG] jugadores.espectadores = %d\n", jugador.espectadores);
            }
        }

        if (lives && lives < fin) {
            // spect: <numero>
            char *col = strchr(lives, ':');
            if (col) {
                jugador.vidas = extraer_num(col + 1);
                printf("[DEBUG] jugadores.vidas = %d\n", jugador.vidas);
            }
        }

    }

    // ===== ENTIDADES =====
    numEntidades = 0;
    char *ent = strstr(json, "\"entidades\"");
    if (ent) {
        char *inicio = strchr(ent, '[');
        char *fin = strchr(ent, ']');
        if (inicio && fin && inicio < fin) {

            char *p = inicio;
            while (p < fin && numEntidades < MAX_ENTIDADES) {

                char *xpos  = strstr(p, "\"x\"");
                char *ypos  = strstr(p, "\"y\"");
                char *typep = strstr(p, "\"tipo\"");
                char *viewp = strstr(p, "\"View\"");

                if (!xpos || !ypos || !typep || !viewp) break;
                if (xpos > fin || ypos > fin || viewp > fin) break;

                entidades[numEntidades].x = extraer_num(xpos + 4) * 20;
                entidades[numEntidades].y = extraer_num(ypos + 4) * 20;
                extraer_string(typep + 6, entidades[numEntidades].type, 20);

                // leer booleano "true"/"false"
                char *val = viewp + 7;
                entidades[numEntidades].view = (*val == 't');

                printf("[DEBUG] entidad[i].view = %d\n", entidades[numEntidades].view);

                numEntidades++;

                // Mover al final del objeto actual
                char *endObj = strchr(viewp, '}');
                if (!endObj) break;
                p = endObj + 1;
            }
        }
    }

    // ===== FRUTAS =====
    numFrutas = 0;
    char *fru = strstr(json, "\"frutas\"");
    if (fru) {
        char *inicio = strchr(fru, '[');
        char *fin = strchr(fru, ']');
        if (inicio && fin && inicio < fin) {
            char *p = inicio;
            while (p < fin && numFrutas < MAX_FRUTAS) {
                char *xpos = strstr(p, "\"x\"");
                char *ypos = strstr(p, "\"y\"");
                char *typep = strstr(p, "\"tipo\"");
                char *pts = strstr(p, "\"puntos\"");

                if (!xpos || !ypos || !typep || !pts || xpos > fin || ypos > fin) break;

                frutas[numFrutas].x = extraer_num(xpos + 4)* 20;
                frutas[numFrutas].y = extraer_num(ypos + 4)* 20;
                extraer_string(typep + 6, frutas[numFrutas].type, 20);
                frutas[numFrutas].points = extraer_num(pts + 9);

                numFrutas++;
                p = ypos + 4;
            }
        }
    }
}

int clientLoop(void *arg) {

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
            return 0;
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
        return 0;
    }
    trim_newline(tipo);

    char sendbuf[BUFFER_SIZE];
    if (strcmp(tipo, "1") == 0) {
        esJugador = 1;
        printf("Nombre del jugador: ");
        if (fgets(nombre, sizeof(nombre), stdin) == NULL) {
            close(sock);
            return 0;
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
        return 0;
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
                return 0;
            }
            buffer[bytes] = '\0';

            printf("Servidor: %s", buffer);

            if (strstr(buffer, "ERROR") != NULL) {
                printf("Error al registrarse. Saliendo...\n");
                terminarEspectador();
                close(sock);
                return 0;
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

            // ======================
            //  MENSAJES DEL SERVIDOR
            // ======================
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

            // ======================
            //  ENTRADA DEL TECLADO
            // ======================
            if (FD_ISSET(STDIN_FILENO, &readfds)) {
                char tecla;
                ssize_t bytes = read(STDIN_FILENO, &tecla, 1);
                if (bytes <= 0) continue;

                char msg[4] = {0};

                // ======================
                // IGNORAR FLECHAS
                // ======================
                if (tecla == 27) { // ESC = inicio de flecha
                    char dump[2];
                    read(STDIN_FILENO, &dump[0], 1); // '['
                    read(STDIN_FILENO, &dump[1], 1); // 'A/B/C/D'
                    continue; // ignoramos completamente
                }

                // ======================
                // WASD
                // ======================
                switch (tecla) {
                    case 'w': case 'W': strcpy(msg, "1"); break;
                    case 'd': case 'D': strcpy(msg, "2"); break;
                    case 's': case 'S': strcpy(msg, "3"); break;
                    case 'a': case 'A': strcpy(msg, "4"); break;
                    case 'r': case 'R': strcpy(msg, "5"); break;
                    case 'q': case 'Q':
                        printf("Saliendo...\n");
                        reset_input_mode();
                        terminar_es = 1;
                        close(sock);
                        return 0;
                    default:
                        continue; // cualquier otra tecla ignorada
                }

                // ======================
                // ANTI-SPAM SIN sleep
                // ======================
                struct timeval now;
                static long last_send_time = 0;

                gettimeofday(&now, NULL);
                long ms = now.tv_sec * 1000 + now.tv_usec / 1000;

                // solo enviar si han pasado 140ms
                if (ms - last_send_time < 140) {
                    continue;
                }
                last_send_time = ms;

                // ======================
                // ENVIAR MENSAJE
                // ======================
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
                terminarEspectador();
                close(sock);
                return 0;
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
            return 0;
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
                terminarEspectador();
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
    terminarEspectador();
    return 0;
}

int main() {

    // --- Inicializar mapa y gráficas ---
    InitGraphics();
    LoadMap("Levels/map.txt");

    thrd_t thread_client;
    thrd_create(&thread_client, clientLoop, NULL);
    thrd_detach(thread_client);

    // --- Loop gráfico principal ---
    while (!WindowShouldClose() && !terminar_es) {
        BeginDrawing();
        ClearBackground(BLACK);


        if (!jugador.dead){
            DrawMap();
            DrawSidePanel(jugador.points, jugador.PlayerName, jugador.espectadores, jugador.vidas);
            // Dibujo del sprite
            if (jugador.climbing) {
                DrawSpriteAt(jr_cu, jugador.x, jugador.y, 3);
            }
            else {
                if (jugador.right) {
                    DrawSpriteAt(jr_b, jugador.x, jugador.y, 3);
                } else {
                    DrawSpriteAt(jr_a, jugador.x, jugador.y, 3);
                }
            }

            if (numEntidades > 0) {
                for (int i = 0; i < numEntidades; i++) {
                    char tipo[20] = "";
                    if (strcmp(entidades[i].type, "ROJO") == 0) { // Es rojo
                        if (entidades[i].view){
                            DrawSpriteAt(CR_d, entidades[i].x, entidades[i].y, 1);
                        }
                        else {
                            DrawSpriteAt(CR_u, entidades[i].x, entidades[i].y, 1);
                        }
                        
                    }
                    else { // Es azul
                        DrawSpriteAt(CB_d, entidades[i].x, entidades[i].y, 1);

                    }
                }
            }

            if (numFrutas > 0) {
                for (int i = 0; i < numFrutas; i++) {
                    if (strcmp(frutas[i].type, "BANANA") == 0) {
                        DrawSpriteAt(f_ban, frutas[i].x, frutas[i].y, 3);
                    }
                    else if (strcmp(frutas[i].type, "STRAWBERRY") == 0)
                    {
                        DrawSpriteAt(f_str, frutas[i].x, frutas[i].y, 3);
                    }
                    else { // Naranja
                        DrawSpriteAt(f_or, frutas[i].x, frutas[i].y, 3);   
                    }
                }            
            }
        }
        else { // Jugador murió
            DrawLose();
        }


        EndDrawing();
    }

    return 0;
}
