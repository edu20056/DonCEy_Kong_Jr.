#include "controller.h"
#include "app/game.h"
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

GameState gameState;
SDL_Window *window;
SDL_Renderer *renderer;

static int running = 1;
// =========================================
// HILO PARA LEER COMANDOS DESDE CONSOLA
// =========================================
void *consoleThread(void *arg) {
    char buffer[128];

    while (running) {

        printf("\nComando > ");
        fflush(stdout);

        if (!fgets(buffer, sizeof(buffer), stdin))
            continue;

        // Quitar salto de línea
        buffer[strcspn(buffer, "\n")] = 0;

        // ===============================================
        // COCODRILOS
        // ===============================================
        if (strncmp(buffer, "croc ", 5) == 0) {

            int x, y;
            if (sscanf(buffer + 5, "%d %d", &x, &y) == 2) {
                addCroc(&gameState, x, y);
                printf(">> Cocodrilo agregado en (%d, %d)\n", x, y);
            } else {
                printf("Uso: croc X Y\n");
            }
        }

        else if (strncmp(buffer, "delcroc ", 8) == 0) {

            int idx;
            if (sscanf(buffer + 8, "%d", &idx) == 1) {
                removeCroc(&gameState, idx);
            } else {
                printf("Uso: delcroc N\n");
            }
        }

        else if (strcmp(buffer, "listcroc") == 0) {

            printf("\n--- COCODRILOS (%d) ---\n", gameState.crocCount);
            for (int i = 0; i < gameState.crocCount; i++) {
                printf("[%d]  x=%d  y=%d\n", i,
                       gameState.crocs[i].x,
                       gameState.crocs[i].y);
            }
            if (gameState.crocCount == 0)
                printf("(ninguno)\n");
        }

        // ===============================================
        // FRUTAS
        // ===============================================
        else if (strncmp(buffer, "fruit ", 6) == 0) {

            int x, y, type;
            if (sscanf(buffer + 6, "%d %d %d", &x, &y, &type) == 3) {

                if (type < 0 || type > 2) {
                    printf("Tipo inválido. Tipos válidos: 0=banana,1=orange,2=strawberry\n");
                } else {
                    addFruit(&gameState, x, y, type);
                    printf(">> Fruta agregada: (%d,%d) tipo=%d\n", x, y, type);
                }

            } else {
                printf("Uso: fruit X Y tipo\n");
            }
        }

        else if (strncmp(buffer, "delfruit ", 9) == 0) {

            int idx;
            if (sscanf(buffer + 9, "%d", &idx) == 1) {
                removeFruit(&gameState, idx);
            } else {
                printf("Uso: delfruit N\n");
            }
        }

        else if (strcmp(buffer, "listfruit") == 0) {

            printf("\n--- FRUTAS (%d) ---\n", gameState.fruitCount);

            for (int i = 0; i < gameState.fruitCount; i++) {
                printf("[%d]  x=%d  y=%d  tipo=%d\n",
                       i,
                       gameState.fruits[i].x,
                       gameState.fruits[i].y,
                       gameState.fruits[i].type);
            }

            if (gameState.fruitCount == 0)
                printf("(ninguna)\n");
        }

        // ===============================================
        // SALIR
        // ===============================================
        else if (strcmp(buffer, "exit") == 0) {
            running = 0;
            printf("Cerrando juego...\n");
            break;
        }

        // ===============================================
        // AYUDA
        // ===============================================
        else {
            printf(
                "\nComandos válidos:\n"
                "  croc X Y          - Crear cocodrilo\n"
                "  delcroc N         - Eliminar cocodrilo\n"
                "  listcroc          - Listar cocodrilos\n"
                "  fruit X Y tipo    - Crear fruta\n"
                "  delfruit N        - Eliminar fruta\n"
                "  listfruit         - Listar frutas\n"
                "  exit              - Salir del juego\n"
            );
        }
    }

    return NULL;
}
// =========================================
// EJECUCIÓN PRINCIPAL DEL JUEGO
// =========================================
void runGame() {

    SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO);
    TTF_Init();
    IMG_Init(IMG_INIT_PNG | IMG_INIT_JPG);

    window = SDL_CreateWindow(
        "DonCEy Kong Jr",
        SDL_WINDOWPOS_CENTERED,
        SDL_WINDOWPOS_CENTERED,
        248*3, 216*3,
        0
    );

    renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);

    gameState.renderer = renderer;
    gameState.crocCount = 0; 
    loadGame(&gameState);

    // ====================================
    // Lanzar hilo que escucha comandos
    // ====================================
    pthread_t tid_console;
    pthread_create(&tid_console, NULL, consoleThread, NULL);

    // ====================================
    // BUCLE PRINCIPAL SDL
    // ====================================
    while (running) {

        if (processEvents(window, &gameState)) break;

        doRender(renderer, &gameState);

        SDL_Delay(16); // ~60 FPS
    }

    running = 0;
    pthread_join(tid_console, NULL);

    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);

    IMG_Quit();
    TTF_Quit();
    Mix_Quit();
    SDL_Quit();
}
