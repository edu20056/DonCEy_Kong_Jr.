#include <stdio.h>
#include <stdlib.h>
#include "controller.h"

int main(int argc, char *argv[]) {
    (void)argc;
    (void)argv;

    printf("=====================================\n");
    printf("      DonCE Y Kong Jr - Modo Local   \n");
    printf("=====================================\n\n");

    runGame();

    return EXIT_SUCCESS;
}



// -------------------------------------------------------
// para compilar, se debe de estar en el directorio de main del cliente: gcc -o DonCEYKongJr     main.c controller.c app/game.c     $(pkg-config --cflags --libs sdl2 SDL2_image SDL2_mixer SDL2_ttf)
// -------------------------------------------------------
//para ejecutar:   ./DonCEYKongJr