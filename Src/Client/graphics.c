#include "graphics.h"
#include <stdio.h>
#include <stdlib.h>

char map[MAP_HEIGHT][MAP_WIDTH + 1];
Texture2D jr_a;
Texture2D jr_b;
Texture2D jr_cu;
Texture2D f_ban;
Texture2D f_or;
Texture2D f_str;
Texture2D CR_d;
Texture2D CR_u;
Texture2D CB_d;
Texture2D CB_u;
Texture2D donko;

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
    InitWindow(MAP_WIDTH * TILE_SIZE + SIDE_PANEL_WIDTH, MAP_HEIGHT * TILE_SIZE, "Mapa DonCEYkong Jr");

    // Tiles
    water    = LoadTexture("Sprites/water.png");
    liana    = LoadTexture("Sprites/liana.png");
    platform = LoadTexture("Sprites/platform.png");
    mario    = LoadTexture("Sprites/mario.png");

    // Jugador
    jr_a     = LoadTexture("Sprites/Jr/dkjr_caminar_izquierda_3.png"); 
    jr_b     = LoadTexture("Sprites/Jr/dkjr_caminar_derecha_3.png"); 
    jr_cu    = LoadTexture("Sprites/Jr/dkjr_esc_2_cuerdas_1.png");
    // Frutas
    f_ban    = LoadTexture("Sprites/Fruits/fruit_bananas.png"); 
    f_or     = LoadTexture("Sprites/Fruits/fruit_oranges.png"); 
    f_str    = LoadTexture("Sprites/Fruits/fruit_strawberry.png"); 

    // Enemigos
    CR_d     = LoadTexture("Sprites/Enemies/Red/kremling_red_d.png"); 
    CR_u     = LoadTexture("Sprites/Enemies/Red/kremling_red_u.png"); 

    CB_d     = LoadTexture("Sprites/Enemies/Blue/kremling_blue_d.png");

    // Donko 
    donko    = LoadTexture("Sprites/Donko/donko.png"); 
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
    for (int y = 0; y < MAP_HEIGHT; y++) {
        for (int x = 0; x < MAP_WIDTH; x++) {

            int px = x * TILE_SIZE;
            int py = y * TILE_SIZE;

            switch (map[y][x]) {
                case '~': DrawTile(water,    px, py); break;
                case 'H': DrawTile(liana,    px, py); break;
                case '=': DrawTile(platform, px, py); break;
                case 'X': DrawTile(mario,    px, py); break;
                case 'D':     
                        // --- Ajustes de posición ---
                        int donko_x  = px - TILE_SIZE;
                        int donko_y  = py - TILE_SIZE;          
                        DrawTexture(donko, donko_x, donko_y, WHITE);
                default: break;
            }
        }
    }
}

// ======================================================
// Dibujar sprite dinámico desde archivo
// ======================================================
void DrawSpriteAt(Texture2D tex, int x_pos, int y_pos, int dir) {

    Rectangle src  = (Rectangle){0, 0, tex.width, tex.height};
    Rectangle dest = (Rectangle){x_pos, y_pos, TILE_SIZE, TILE_SIZE};

    DrawTexturePro(tex, src, dest, (Vector2){0, 0}, 0.0f, WHITE);
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

// ======================================================
// Dibuja panel donde se ven los puntos de jugador
// ======================================================
void DrawSidePanel(int points, const char *nombre, int spect, int lives) {
    int panelX = MAP_WIDTH * TILE_SIZE;  // Donde empieza el panel

    // Fondo del panel
    DrawRectangle(panelX, 0, SIDE_PANEL_WIDTH, MAP_HEIGHT * TILE_SIZE, DARKGRAY);

    // ====== Nombre del jugador ======
    DrawText("Nombre Jugador", panelX + 20, 20, 22, RAYWHITE);
    DrawText(nombre,            panelX + 20, 55, 28, YELLOW);

    // ====== Puntos ======
    DrawText("Puntos:", panelX + 20, 110, 22, RAYWHITE);

    char buffer[16];
    snprintf(buffer, sizeof(buffer), "%d", points);
    DrawText(buffer, panelX + 20, 140, 28, YELLOW);

    // ====== Espectadores ======
    DrawText("Espectadores", panelX + 20, 200, 22, RAYWHITE);

    snprintf(buffer, sizeof(buffer), "%d", spect);
    DrawText(buffer, panelX + 20, 230, 28, YELLOW);

    // ====== Vidas ======
    DrawText("Vidas de jugador", panelX + 20, 290, 22, RAYWHITE);

    snprintf(buffer, sizeof(buffer), "%d", lives);
    DrawText(buffer, panelX + 20, 320, 28, YELLOW);
}


// ======================================================
// Dibuja pantalla de pérdida
// ======================================================
void DrawLose() {
    const int boxWidth  = 600;
    const int boxHeight = 200;

    int centerX = (MAP_WIDTH * TILE_SIZE) / 2;
    int centerY = (MAP_HEIGHT * TILE_SIZE) / 2;

    int boxX = centerX - boxWidth / 2;
    int boxY = centerY - boxHeight / 2;

    // Fondo del recuadro
    DrawRectangle(boxX, boxY, boxWidth, boxHeight, Fade(BLACK, 0.8f));

    // Borde
    DrawRectangleLines(boxX, boxY, boxWidth, boxHeight, RAYWHITE);

    // Texto
    DrawText("El jugador ha muerto", 
             boxX + 40, 
             boxY + 50, 
             30, 
             RAYWHITE);

    DrawText("Presione Q para salir o R para reiniciar", 
             boxX + 40, 
             boxY + 110, 
             20, 
             RAYWHITE);
}

