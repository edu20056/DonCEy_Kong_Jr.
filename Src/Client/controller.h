#ifndef DONCEYKONGJR_CONTROLLER_H
#define DONCEYKONGJR_CONTROLLER_H

#include <SDL2/SDL.h>
#include <SDL2/SDL_image.h>
#include <SDL2/SDL_ttf.h>
#include <SDL2/SDL_mixer.h>


#include "app/game.h"

// Variables globales 
extern GameState gameState;
extern SDL_Window *window;
extern SDL_Renderer *renderer;

// Función principal del juego
void runGame();

#endif // DONCEYKONGJR_CONTROLLER_H
