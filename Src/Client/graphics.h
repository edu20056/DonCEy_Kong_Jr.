#ifndef GRAPHICS_H
#define GRAPHICS_H

#include "raylib.h"

#define TILE_SIZE 20
#define MAP_WIDTH  33
#define MAP_HEIGHT 22

extern char map[MAP_HEIGHT][MAP_WIDTH + 1];

void LoadMap(const char *filename);
void InitGraphics();
void DrawMap();
void CloseGraphics();

#endif
