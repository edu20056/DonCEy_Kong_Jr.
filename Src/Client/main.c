#include "client.h"
#include "graphics.h"
#include <pthread.h>

int main() {

    // --- Inicializar mapa y gráficas ---
    LoadMap("Levels/map.txt");
    InitGraphics();

    // --- Lanzar el cliente en un hilo ---
    pthread_t thread_client;
    if (pthread_create(&thread_client, NULL, run_client, NULL) != 0) {
        perror("Error creando hilo run_client");
        return 1;
    }

    // Opcional: dejar que el hilo se maneje solo
    pthread_detach(thread_client);

    // --- Loop gráfico principal ---
    while (!WindowShouldClose()) {
        BeginDrawing();
        ClearBackground(BLACK);

        DrawMap();

        EndDrawing();
    }

    return 0;
}
