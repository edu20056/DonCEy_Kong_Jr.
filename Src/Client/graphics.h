#ifndef GRAPHICS_H
#define GRAPHICS_H

#include "raylib.h"

#define TILE_SIZE 20
#define MAP_WIDTH  33
#define MAP_HEIGHT 22
#define SIDE_PANEL_WIDTH 240

extern Texture2D jr_a;
extern Texture2D jr_b;
extern Texture2D jr_cu;
extern Texture2D f_ban;
extern Texture2D f_or;
extern Texture2D f_str;
extern Texture2D CR_d;
extern Texture2D CR_u;
extern Texture2D CB_d;
extern Texture2D CB_u;
extern char map[MAP_HEIGHT][MAP_WIDTH + 1];

void LoadMap(const char *filename);
void InitGraphics();
void DrawMap();
void CloseGraphics();
void DrawSpriteAt(Texture2D tex, int x_pos, int y_pos, int dir);
void DrawSidePanel(int points, const char *nombre, int spect);
void DrawLose(void);


#endif
