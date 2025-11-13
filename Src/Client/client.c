#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/select.h>
#include <termios.h>
#include <sys/socket.h>
#include <arpa/inet.h>

#define BUFFER_SIZE 1024
#define SERVER_IP "127.0.0.1"    
#define SERVER_PORT 5000  

void trim_newline(char *str) {
    size_t len = strlen(str);
    while (len > 0 && (str[len - 1] == '\n' || str[len - 1] == '\r')) {
        str[--len] = '\0';
    }
}

// Funciones para modo sin buffer
void set_input_mode(void) {
    struct termios tattr;

    // Obtener configuración actual
    tcgetattr(STDIN_FILENO, &tattr);

    // Desactivar modo canónico y eco (no requiere Enter ni muestra las teclas)
    tattr.c_lflag &= ~(ICANON | ECHO);
    tattr.c_cc[VMIN] = 1;
    tattr.c_cc[VTIME] = 0;

    // Aplicar configuración
    tcsetattr(STDIN_FILENO, TCSANOW, &tattr);
}

void reset_input_mode(void) {
    struct termios tattr;
    tcgetattr(STDIN_FILENO, &tattr);
    tattr.c_lflag |= ICANON | ECHO;
    tcsetattr(STDIN_FILENO, TCSANOW, &tattr);
}

int main() {
    int sock;
    struct sockaddr_in server_addr;
    char buffer[BUFFER_SIZE];
    char tipo[20];
    char nombre[50];
    int esJugador = 0;

    // Crear socket
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
            return 1;
        }
        buffer[bytes] = '\0';
        
        // Mostrar mensaje del servidor
        printf("%s", buffer);
        
        // Verificar si terminó la bienvenida
        if (strstr(buffer, "Conexión Exitosa.") != NULL) {
            bienvenida_completa = 1;
        }
    }

    // Ahora preguntar al usuario qué tipo quiere ser
    printf("\nSeleccione tipo de cliente:\n");
    printf("1. Jugador\n");
    printf("2. Espectador\n");
    printf("> ");
    
    if (fgets(tipo, sizeof(tipo), stdin) == NULL) {
        close(sock);
        return 1;
    }
    trim_newline(tipo);

    char sendbuf[BUFFER_SIZE];
    if (strcmp(tipo, "1") == 0) {
        esJugador = 1;
        printf("Nombre del jugador: ");
        if (fgets(nombre, sizeof(nombre), stdin) == NULL) {
            close(sock);
            return 1;
        }
        trim_newline(nombre);
        snprintf(sendbuf, sizeof(sendbuf), "PLAYER %s\n", nombre);
    } 
    else if (strcmp(tipo, "2") == 0) {
        esJugador = 0;
        snprintf(sendbuf, sizeof(sendbuf), "SPECTATOR\n");
    } 
    else {
        printf("Opción inválida. Debe ingresar 1 (Jugador) o 2 (Espectador).\n");
        close(sock);
        return 1;
    }
    // Enviar tipo de cliente al servidor
    printf("Enviando tipo de cliente al servidor: %s", sendbuf);
    send(sock, sendbuf, strlen(sendbuf), 0);

    if (esJugador) {
        // Modo jugador - esperar confirmación
        printf("Esperando confirmación del servidor...\n");
        
        // Leer toda la respuesta del servidor
            while (1) {
                ssize_t bytes = recv(sock, buffer, sizeof(buffer) - 1, 0);
                if (bytes <= 0) {
                    printf("Error del servidor o conexión cerrada\n");
                    close(sock);
                    return 1;
                }
                buffer[bytes] = '\0';
                
                printf("Servidor: %s", buffer);
                
                // Si recibimos un error, salir
                if (strstr(buffer, "ERROR") != NULL) {
                    printf("Error al registrarse. Saliendo...\n");
                    close(sock);
                    return 1;
                }
                
                // Si recibimos el mensaje de confirmación, continuar
                if (strstr(buffer, "OK:") != NULL || strstr(buffer, "Puede comenzar") != NULL) {
                    break;
                }
            }
            
            printf("Use las teclas W, A, S, D para moverse. (Escriba 'quit' para salir)\n");
            printf("> ");
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

                // Mensaje del servidor
                if (FD_ISSET(sock, &readfds)) {
                    ssize_t bytes = recv(sock, buffer, sizeof(buffer) - 1, 0);
                    if (bytes <= 0) {
                        printf("\nServidor desconectado.\n");
                        break;
                    }
                    buffer[bytes] = '\0';
                    printf("\n%s\n> ", buffer);
                    fflush(stdout);
                }

                // Lectura de tecla (sin Enter)
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
                            reset_input_mode(); // restaurar terminal
                            close(sock);
                            return 0;
                        default:
                            continue; // Ignorar otras teclas
                    }

                    strcat(msg, "\n");
                    send(sock, msg, strlen(msg), 0);
                }
            }

    // Restaurar modo normal al salir
    reset_input_mode();
    } else {
        // Modo espectador - VERSIÓN SIMPLIFICADA
        printf("Modo espectador seleccionado.\n");
        
        // Recibir datos hasta encontrar END_PLAYERS_LIST
        char total_buffer[BUFFER_SIZE * 4] = {0}; // Buffer más grande
        int lista_completa = 0;
        
        while (!lista_completa) {
            ssize_t bytes = recv(sock, buffer, sizeof(buffer) - 1, 0);
            if (bytes <= 0) {
                printf("Error: No se pudo recibir lista de jugadores\n");
                close(sock);
                return 1;
            }
            buffer[bytes] = '\0';
            strcat(total_buffer, buffer);
            
            if (strstr(total_buffer, "END_PLAYERS_LIST") != NULL) {
                lista_completa = 1;
            }
            
            // Timeout de seguridad
            usleep(100000); // 100ms
        }
        
        // Mostrar solo la parte de la lista de jugadores
        char *start_list = strstr(total_buffer, "Jugadores disponibles:");
        char *end_list = strstr(total_buffer, "END_PLAYERS_LIST");
        
        if (start_list && end_list) {
            *end_list = '\0';
            printf("%s\n", start_list);
        } else {
            printf("Lista recibida:\n%s\n", total_buffer);
        }
        
        // Resto del código igual...
        printf("Ingrese el numero de jugador por espectear: ");
        if (fgets(nombre, sizeof(nombre), stdin) == NULL) {
            close(sock);
            return 1;
        }
        trim_newline(nombre);
        
        // Enviar nombre del jugador al servidor CON newline
        char nombre_con_newline[BUFFER_SIZE * 2 ];
        snprintf(nombre_con_newline, sizeof(nombre_con_newline), "%s\n", nombre);
        send(sock, nombre_con_newline, strlen(nombre_con_newline), 0);
        
        // Esperar confirmación y recibir mensajes
        printf("Esperando confirmación del servidor...\n");
        
        while (1) {
            ssize_t bytes = recv(sock, buffer, sizeof(buffer) - 1, 0);
            if (bytes <= 0) {
                printf("Conexión con el servidor terminada.\n");
                break;
            }
            buffer[bytes] = '\0';
            
            // Si es un error, salir
            if (strstr(buffer, "ERROR") != NULL) {
                printf("Error: %s\n", buffer);
                break;
            }
            
            // Si es confirmación, mostrar y continuar
            if (strstr(buffer, "OK:") != NULL || strstr(buffer, "Conectado") != NULL) {
                printf("Servidor: %s", buffer);
                printf("\n=== Modo Espectador activado ===\n");
                printf("Recibiendo mensajes...\n\n");
                continue;
            }
            
            // Mostrar mensajes normales
            printf("%s", buffer);
        }
    }

    close(sock);
    printf("Conexión cerrada.\n");
    return 0;
}