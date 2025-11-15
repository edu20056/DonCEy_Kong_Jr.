#ifndef DONCEYKONGJR_GAME_H
#define DONCEYKONGJR_GAME_H

#include <SDL2/SDL.h>
#include <SDL2/SDL_image.h>
#include <SDL2/SDL_mixer.h>
#include <SDL2/SDL_ttf.h>

#define MAX_CROCS 50
#define MAX_LIANAS 20
#define MAX_FRUITS 50
// ------------------------------------
// Jugador básico
// ------------------------------------
typedef struct {
    int x, y;
} Player;
// ------------------------------------
// Cocodrilos agregados por consola
// ------------------------------------
typedef struct {
    int x, y, w, h;
} Crocodile;
// ------------------------------------
// Frutas agregadas por consola
// ------------------------------------
typedef struct {
    int x, y, w, h;
    int type;   // 0 = banana, 1 = orange, 2 = strawberry
} Fruit;
// ------------------------------------
// Lianas
// ------------------------------------
typedef struct {
    int x, y, w, h;
} Liana;

// ------------------------------------
// Safety Key
// ------------------------------------
typedef struct {
    int x, y, w;
} SafetyKey;

typedef struct {
    int x, y, w, h;
} Ledge;

typedef struct {
    int x, y, w, h;
} UnderLedge;
// ------------------------------------
// Estado general del juego
// ------------------------------------
typedef struct {

    SDL_Renderer *renderer;

    // Texturas principales
    SDL_Texture *background;
    SDL_Texture *menu;
    SDL_Texture *playerIdle;

    // Mario, DK, Jaula, Scoreholder, Hearts
    SDL_Texture *mario;
    SDL_Texture *dk;
    SDL_Texture *jail;
    SDL_Texture *scoreholder;
    SDL_Texture *heart;

    // Safety Key
    SDL_Texture *safetyKey;
    SafetyKey safekey;

    // Plataformas
    SDL_Texture *brick;       
    SDL_Texture *platform;    
    Ledge ledges[100];
    UnderLedge underledges[100];

    // Lianas
    SDL_Texture *liana;
    Liana lianas[MAX_LIANAS];
    int lianaCount;

    // Cocodrilos
    SDL_Texture *crocTexture;
    Crocodile crocs[MAX_CROCS];
    int crocCount;

    // Frutas
    SDL_Texture *bananaTex;
    SDL_Texture *orangeTex;
    SDL_Texture *strawberryTex;

    Fruit fruits[MAX_FRUITS];
    int fruitCount;

    // Música
    Mix_Music *bgMusic;
    Mix_Music *openingMusic;

    // Estado del juego
    int windowPage; // 0 = menú, 1 = juego

    // Jugador
    Player player;

} GameState;
// ------------------------------------
// Funciones
// ------------------------------------
void loadGame(GameState *game);
void doRender(SDL_Renderer *renderer, GameState *game);
int processEvents(SDL_Window *window, GameState *game);

// Cocodrilos
void addCroc(GameState *game, int x, int y);
void removeCroc(GameState *game, int index);

// Frutas
void addFruit(GameState *game, int x, int y, int type);
void removeFruit(GameState *game, int index);

#endif
