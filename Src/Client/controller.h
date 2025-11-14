#ifndef DONCEYKONGJR_CONTROLLER_H
#define DONCEYKONGJR_CONTROLLER_H

#include "app/game.h"
#include <pthread.h>
#include <SDL2/SDL.h>
#include <SDL2/SDL_image.h>
#include <SDL2/SDL_ttf.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

// Declarar variables globales externas
extern GameState gameState;
extern SDL_Window *window;
extern SDL_Renderer *renderer;
extern Lists *lists;  

void runGame();
void updateEnemies(void);



#endif //DONCEYKONGJR_CONTROLLER_H