#include "controller.h"
#include <string.h>

// Variables globales del controller
GameState gameState;
SDL_Window *window;
SDL_Renderer *renderer;
void updateEnemies(void);

void runGame() {
    printf("Debug: Iniciando runGame()\n");

    // INICIALIZAR lists
    lists = malloc(sizeof(Lists));
    if (lists == NULL) {
        printf("Error: No se pudo asignar memoria para lists\n");
        return;
    }
    memset(lists, 0, sizeof(Lists));

    lists->gameOn = 1;
    lists->score = 0;
    lists->hp = 1;
    lists->commOn = 0;

    // --- Entrada dinámica de cocodrilos ---
    printf("Ingrese la cantidad de cocodrilos: ");
    scanf("%d", &lists->numOfCrocodiles);
    if(lists->numOfCrocodiles > 100) lists->numOfCrocodiles = 100;
    lists->currentNumberOfCrocodiles = lists->numOfCrocodiles;

    for (int i = 0; i < lists->numOfCrocodiles; i++) {
        int x, y, species;
        printf("Cocodrilo %d - ingrese posX posY especie(0=azul,1=rojo): ", i+1);
        scanf("%d %d %d", &x, &y, &species);

        lists->cocrodileList[i].posX = x;
        lists->cocrodileList[i].posY = y;
        lists->cocrodileList[i].species = species % 2;
        lists->cocrodileList[i].speed = 2;
        lists->cocrodileList[i].alive = 1;
        lists->cocrodileList[i].eCollider.x = x;
        lists->cocrodileList[i].eCollider.y = y;
        lists->cocrodileList[i].eCollider.w = 40;
        lists->cocrodileList[i].eCollider.h = 40;
    }

    // --- Inicialización de frutas ---
    lists->numOfFruits = 8;
    lists->currentNumberOfFruits = lists->numOfFruits;
    for (int i = 0; i < lists->numOfFruits; i++) {
        lists->fruitList[i].posX = 150 + i * 80;
        lists->fruitList[i].posY = 300 - (i % 3) * 50;
        lists->fruitList[i].species = i % 3;
        lists->fruitList[i].score = (lists->fruitList[i].species + 1) * 100;
        lists->fruitList[i].alive = 1;
        lists->fruitList[i].eCollider.x = lists->fruitList[i].posX;
        lists->fruitList[i].eCollider.y = lists->fruitList[i].posY;
        lists->fruitList[i].eCollider.w = 40;
        lists->fruitList[i].eCollider.h = 40;
    }

    printf("Debug: Enemigos y frutas inicializados\n");

    // Inicializar SDL
    if (SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO) < 0) {
        printf("Error al inicializar SDL: %s\n", SDL_GetError());
        return;
    }
    if (TTF_Init() < 0) {
        printf("Error al inicializar TTF: %s\n", TTF_GetError());
        return;
    }
    int flags = IMG_INIT_JPG | IMG_INIT_PNG;
    int initted = IMG_Init(flags);
    if ((initted & flags) != flags) {
        printf("Error al inicializar IMG: %s\n", IMG_GetError());
        return;
    }

    srand((int)time(NULL));

    // Crear ventana y renderer
    window = SDL_CreateWindow("DonCE Y Kong Jr - Local",
                              SDL_WINDOWPOS_UNDEFINED,
                              SDL_WINDOWPOS_UNDEFINED,
                              248 * 3,
                              216 * 3,
                              SDL_WINDOW_SHOWN);
    if (!window) {
        printf("Error al crear ventana: %s\n", SDL_GetError());
        return;
    }

    renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED | SDL_RENDERER_PRESENTVSYNC);
    if (!renderer) {
        printf("Error al crear renderer: %s\n", SDL_GetError());
        return;
    }

    gameState.renderer = renderer;
    loadGame(&gameState);

    // --- Bucle principal de juego y renderizado ---
    SDL_Event event;
    int running = 1;

    printf("Debug: Iniciando bucle principal de juego...\n");
    while (running && lists->gameOn) {
        Uint32 frameStart = SDL_GetTicks();

        // Procesar eventos
        int done = processEvents(window, &gameState);
        if (done) running = 0;

        // Lógica del juego
        updateEnemies();
        process(&gameState);
        collisionDetect(&gameState);
        ObjectCollision(&gameState);

        // Renderizado
        doRender(renderer, &gameState);

        // Control de FPS ~60
        Uint32 frameTime = SDL_GetTicks() - frameStart;
        if(frameTime < 16) SDL_Delay(16 - frameTime);
    }

    // --- Liberar recursos ---
    printf("Debug: Cerrando juego y liberando recursos\n");
    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);
    IMG_Quit();
    TTF_Quit();
    SDL_Quit();
    free(lists);
}
