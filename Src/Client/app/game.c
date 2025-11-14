#include "game.h"
#include <stdio.h>

// -------------------------------------------------------
// CARGA DE TEXTURAS Y SONIDOS
// -------------------------------------------------------
void loadGame(GameState *game)
{
    SDL_Surface *surface;

    // ===== Fondo =====
    surface = IMG_Load("img/background.png");
    if (!surface) { printf("Falta background.png\n"); exit(1); }
    game->background = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    // ===== Menú =====
    surface = IMG_Load("img/menu2.png");
    if (!surface) { printf("Falta menu2.png\n"); exit(1); }
    game->menu = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    // ===== Jugador =====
    surface = IMG_Load("img/jr_a.png");
    if (!surface) { printf("Falta jr_a.png\n"); exit(1); }
    game->playerIdle = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    // ===== Cocodrilo azul =====
    surface = IMG_Load("img/kremling_blue_d.png");
    if (!surface) { printf("Falta kremling_blue_d.png\n"); exit(1); }
    game->crocTexture = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);
    game->crocCount = 0;

    // ======== FRUTAS ========
    surface = IMG_Load("img/fruit_bananas.png");
    game->bananaTex = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    surface = IMG_Load("img/fruit_oranges.png");
    game->orangeTex = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    surface = IMG_Load("img/fruit_strawberry.png");
    game->strawberryTex = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    game->fruitCount = 0;

    // ===== Plataforma superior (brick) =====
    surface = IMG_Load("img/platform.png");
    if (!surface) { printf("Falta platform.png\n"); exit(1); }
    game->brick = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    for (int i = 0; i < 100; i++) {
        game->ledges[i].w = 0; // por defecto invisible
    }

    game->ledges[94] = (Ledge){552, 368, 190, 22};
    game->ledges[95] = (Ledge){405, 176, 195, 22};
    game->ledges[96] = (Ledge){190,  80,  75, 22};
    game->ledges[97] = (Ledge){0,   155, 432, 22};
    game->ledges[98] = (Ledge){120, 295,  96, 22};
    game->ledges[99] = (Ledge){120, 415, 145, 22};

    // ===== Plataforma inferior =====
    surface = IMG_Load("img/downplatform.png");
    if (!surface) { printf("Falta downplatform.png\n"); exit(1); }
    game->platform = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    for (int i = 0; i < 100; i++) {
        game->underledges[i].w = 0;
    }

    game->underledges[95] = (UnderLedge){600, 530, 100, 200};
    game->underledges[96] = (UnderLedge){480, 550,  95, 140};
    game->underledges[97] = (UnderLedge){380, 585,  80, 140};
    game->underledges[98] = (UnderLedge){260, 550, 100, 140};
    game->underledges[99] = (UnderLedge){0,   618, 188, 100};

    // ===== Mario =====
    surface = IMG_Load("img/mario.png");
    game->mario = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    // ===== Donkey Kong =====
    surface = IMG_Load("img/dk.png");
    game->dk = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    // ===== Jaula =====
    surface = IMG_Load("img/jail.png");
    game->jail = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    // ===== Scoreholder =====
    surface = IMG_Load("img/scoreholder.png");
    game->scoreholder = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    // ===== Hearts =====
    surface = IMG_Load("img/heart.png");
    game->heart = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    // ===== Llave =====
    surface = IMG_Load("img/safetkey.png");
    game->safetyKey = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    game->safekey.x = 200;
    game->safekey.y = 30;
    game->safekey.w = 50;

    // ===== Lianas =====
    surface = IMG_Load("img/liana.png");
    game->liana = SDL_CreateTextureFromSurface(game->renderer, surface);
    SDL_FreeSurface(surface);

    game->lianaCount = 10;

    game->lianas[0] = (Liana){ 297,  52, 5, 50  };
    game->lianas[1] = (Liana){ 681, 105, 5, 385 };
    game->lianas[2] = (Liana){ 609, 105, 5, 380 };
    game->lianas[3] = (Liana){ 537, 175, 5, 260 };
    game->lianas[4] = (Liana){ 465, 175, 5, 300 };
    game->lianas[5] = (Liana){ 393, 150, 5, 240 };
    game->lianas[6] = (Liana){ 297, 150, 5, 330 };
    game->lianas[7] = (Liana){ 177, 295, 5, 268 };
    game->lianas[8] = (Liana){ 105, 150, 5, 390 };
    game->lianas[9] = (Liana){  34, 150, 5, 410 };

    // ===== AUDIO =====
    Mix_Init(MIX_INIT_MP3);
    Mix_OpenAudio(44100, MIX_DEFAULT_FORMAT, 2, 2048);

    game->openingMusic = Mix_LoadMUS("audio/03_Opening.mp3");
    game->bgMusic      = Mix_LoadMUS("audio/02_Stage1.mp3");

    Mix_PlayMusic(game->openingMusic, -1);

    // ===============================
    // POSICIÓN INICIAL DEL JUGADOR
    // ===============================
    game->player.x = 40;
    game->player.y = 500;   


    game->windowPage = 0;
}


// -------------------------------------------------------
// AGREGAR COCODRILO
// -------------------------------------------------------
void addCroc(GameState *game, int x, int y)
{
    if (game->crocCount >= MAX_CROCS) {
        printf("Máximo de cocodrilos alcanzado.\n");
        return;
    }

    Crocodile *c = &game->crocs[game->crocCount];
    c->x = x;
    c->y = y;
    c->w = 40;
    c->h = 40;

    game->crocCount++;

    printf(">> Cocodrilo agregado en (%d, %d)\n", x, y);
}

// -------------------------------------------------------
// ELIMINAR COCODRILO POR ÍNDICE
// -------------------------------------------------------
void removeCroc(GameState *game, int index)
{
    if (index < 0 || index >= game->crocCount) {
        printf("Índice inválido.\n");
        return;
    }

    game->crocs[index] = game->crocs[game->crocCount - 1];
    game->crocCount--;

    printf(">> Cocodrilo %d eliminado\n", index);
}



// -------------------------------------------------------
// AGREGAR FRUTA
// -------------------------------------------------------
void addFruit(GameState *game, int x, int y, int type)
{
    if (game->fruitCount >= MAX_FRUITS) {
        printf("Máximo de frutas alcanzado.\n");
        return;
    }

    Fruit *f = &game->fruits[game->fruitCount];
    f->x = x;
    f->y = y;
    f->w = 40;
    f->h = 40;
    f->type = type % 3;

    game->fruitCount++;

    printf(">> Fruta agregada (%d,%d) tipo=%d\n", x, y, type);
}

void removeFruit(GameState *game, int index)
{
    if (index < 0 || index >= game->fruitCount) {
        printf("Índice inválido.\n");
        return;
    }

    game->fruits[index] = game->fruits[game->fruitCount - 1];
    game->fruitCount--;

    printf(">> Fruta %d eliminada\n", index);
}



// -------------------------------------------------------
// PROCESO DE EVENTOS
// -------------------------------------------------------
int processEvents(SDL_Window *window, GameState *game)
{
    SDL_Event event;

    while (SDL_PollEvent(&event)) {

        if (event.type == SDL_QUIT)
            return 1;

        if (event.type == SDL_MOUSEBUTTONDOWN) {
            if (game->windowPage == 0) {
                Mix_HaltMusic();
                Mix_PlayMusic(game->bgMusic, -1);
                game->windowPage = 1;
            }
        }
    }

    return 0;
}
// -------------------------------------------------------
// RENDER
// -------------------------------------------------------
void doRender(SDL_Renderer *renderer, GameState *game)
{
    SDL_SetRenderDrawColor(renderer, 0, 0, 0, 255);
    SDL_RenderClear(renderer);

    // ============================
    //        MENÚ PRINCIPAL
    // ============================
    if (game->windowPage == 0) {
        SDL_Rect r = {0, 0, 248*3, 216*3};
        SDL_RenderCopy(renderer, game->menu, NULL, &r);
    }

    // ============================
    //            JUEGO
    // ============================
    else if (game->windowPage == 1) {

        // Fondo
        SDL_Rect bg = {0, 0, 248*3, 216*3};
        SDL_RenderCopy(renderer, game->background, NULL, &bg);

        // =====================================================
        //        1. PLATAFORMAS 
        // =====================================================
        for (int i = 0; i < 100; i++) {
            if (game->ledges[i].w > 0) {
                SDL_Rect r = {
                    game->ledges[i].x,
                    game->ledges[i].y,
                    game->ledges[i].w,
                    game->ledges[i].h
                };
                SDL_RenderCopy(renderer, game->brick, NULL, &r);
            }
        }

        for (int i = 0; i < 100; i++) {
            if (game->underledges[i].w > 0) {
                SDL_Rect r = {
                    game->underledges[i].x,
                    game->underledges[i].y,
                    game->underledges[i].w,
                    game->underledges[i].h
                };
                SDL_RenderCopy(renderer, game->platform, NULL, &r);
            }
        }

        // =====================================================
        //        2. LIANAS
        // =====================================================
        for (int i = 0; i < game->lianaCount; i++) {
            SDL_Rect L = {
                game->lianas[i].x,
                game->lianas[i].y,
                game->lianas[i].w,
                game->lianas[i].h
            };
            SDL_RenderCopy(renderer, game->liana, NULL, &L);
        }

        // =====================================================
        //        3. SAFETY KEY
        // =====================================================
        SDL_Rect keyRect = {
            game->safekey.x,
            game->safekey.y,
            game->safekey.w,
            game->safekey.w
        };
        SDL_RenderCopy(renderer, game->safetyKey, NULL, &keyRect);

        // =====================================================
        //        4. FRUTAS DESDE CONSOLA
        // =====================================================
        for (int i = 0; i < game->fruitCount; i++) {
            SDL_Rect fr = {
                game->fruits[i].x,
                game->fruits[i].y,
                game->fruits[i].w,
                game->fruits[i].h
            };
            switch (game->fruits[i].type) {
                case 0: SDL_RenderCopy(renderer, game->bananaTex, NULL, &fr); break;
                case 1: SDL_RenderCopy(renderer, game->orangeTex, NULL, &fr); break;
                case 2: SDL_RenderCopy(renderer, game->strawberryTex, NULL, &fr); break;
            }
        }

        // =====================================================
        //        5. COCODRILOS DESDE CONSOLA
        // =====================================================
        for (int i = 0; i < game->crocCount; i++) {
            SDL_Rect r = {
                game->crocs[i].x,
                game->crocs[i].y,
                game->crocs[i].w,
                game->crocs[i].h
            };
            SDL_RenderCopy(renderer, game->crocTexture, NULL, &r);
        }

        // =====================================================
        //        6. HUBICACIÓN FIJA DE MARIO, DK, JAULA, SCOREHOLDER, HEARTS
        // =====================================================
        SDL_Rect marioRect = {175, 100, 75, 75};
        SDL_RenderCopy(renderer, game->mario, NULL, &marioRect);

        SDL_Rect jailRect = {20, 50, 150, 100};
        SDL_RenderCopy(renderer, game->jail, NULL, &jailRect);

        SDL_Rect dkRect = {65, 60, 70, 65};
        SDL_RenderCopy(renderer, game->dk, NULL, &dkRect);

        SDL_Rect scoreholderRect = {540, 0, 200, 100};
        SDL_RenderCopy(renderer, game->scoreholder, NULL, &scoreholderRect);

        // Hearts
        for (int j = 0; j < 3; j++) {
            SDL_Rect heartR = {370 + j*54, 5, 40, 40};
            SDL_RenderCopy(renderer, game->heart, NULL, &heartR);
        }

        // =====================================================
        //        7. JUGADOR 
        // =====================================================
        SDL_Rect p = {game->player.x, game->player.y, 70, 70};
        SDL_RenderCopy(renderer, game->playerIdle, NULL, &p);
    }

    SDL_RenderPresent(renderer);
}
