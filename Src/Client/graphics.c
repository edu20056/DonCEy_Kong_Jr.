#include "graphics.h"
#include <stdio.h>
#include <stdlib.h>

char map[MAP_HEIGHT][MAP_WIDTH + 1];

// ======================================================
// Cargar mapa desde archivo
// ======================================================
void LoadMap(const char *filename) {
    FILE *f = fopen(filename, "r");
    if (!f) {
        printf("Error abriendo %s\n", filename);
        exit(1);
    }

    for (int y = 0; y < MAP_HEIGHT; y++) {
        fgets(map[y], MAP_WIDTH + 2, f);

        for (int i = 0; i < MAP_WIDTH + 2; i++) {
            if (map[y][i] == '\n' || map[y][i] == '\r') {
                map[y][i] = '\0';
                break;
            }
        }
    }

    fclose(f);
}

// ======================================================
// Texturas
// ======================================================
static Texture2D water;
static Texture2D liana;
static Texture2D platform;
static Texture2D mario;

// ======================================================
// Inicializar la ventana y texturas
// ======================================================
void InitGraphics() {
    InitWindow(MAP_WIDTH * TILE_SIZE, MAP_HEIGHT * TILE_SIZE, "Mapa DonCEYkong Jr");

    water    = LoadTexture("Sprites/water.png");
    liana    = LoadTexture("Sprites/liana.png");
    platform = LoadTexture("Sprites/platform.png");
    mario    = LoadTexture("Sprites/mario.png");
}

// ======================================================
// Dibujar un tile a 20x20
// ======================================================
static void DrawTile(Texture2D tex, int x, int y) {
    Rectangle src  = { 0, 0, tex.width, tex.height };
    Rectangle dest = { x, y, TILE_SIZE, TILE_SIZE };
    DrawTexturePro(tex, src, dest, (Vector2){0, 0}, 0.0f, WHITE);
}

// ======================================================
// Dibujar todo el mapa
// ======================================================
void DrawMap() {
    BeginDrawing();
    ClearBackground(BLACK);

    for (int y = 0; y < MAP_HEIGHT; y++) {
        for (int x = 0; x < MAP_WIDTH; x++) {

            int px = x * TILE_SIZE;
            int py = y * TILE_SIZE;

            switch (map[y][x]) {
                case '~': DrawTile(water,    px, py); break;
                case 'H': DrawTile(liana,    px, py); break;
                case '=': DrawTile(platform, px, py); break;
                case 'X': DrawTile(mario,    px, py); break;
                default: break;
            }
        }
    }

    EndDrawing();
}

// ======================================================
// Cerrar ventana y liberar texturas
// ======================================================
void CloseGraphics() {
    UnloadTexture(water);
    UnloadTexture(liana);
    UnloadTexture(platform);
    UnloadTexture(mario);
    CloseWindow();
}
