/*
 * Let's consider the map measured in meters.
 * 
 * The map is 1024x1024 height points. It is split into 16x16 tiles. Each tile
 * should have 64x64 points, but the need to fit together so each tile is padded
 * on the left and bottom with the points of the following tile so each tile
 * has 65x65 points (or 64x64 squares). Now, block is equivalent to tile, and
 * cell width is equivalent to the distance between two points (i.e. horizontal
 * scale).
 */

Config = {};
Config.host = document.location.hostname;
Config.port = 8000;
Config.FOV = 45;
Config.CAMERA_ANGLE = 50;
Config.CAMERA_DISTANCE = 160;
Config.MAP_SEGMENTS = 1024;
Config.MAP_TILES = 16;
Config.HORIZ_SCALE = 10;
Config.VERT_SCALE = 0.008;
Config.VERT_OFFSET = -190;
Config.CELL = Config.HORIZ_SCALE;
Config.HALF_CELL = Config.CELL / 2;
Config.TILE_SEGMENTS = Config.MAP_SEGMENTS / Config.MAP_TILES;
Config.TILE_POINTS = Config.TILE_SEGMENTS + 1;
Config.MAP_SIZE = Config.MAP_SEGMENTS * Config.HORIZ_SCALE;
Config.TILE_SIZE = Config.TILE_SEGMENTS * Config.HORIZ_SCALE;
Config.ARENAS = {
	"forest": {x: 20000, y: 20000},
	"desert": {x: 20000, y: 40000}
};

MsgFrom = {};
//MsgFrom.LOGIN_OR_REGISTER = 1; // Never received by client.
MsgFrom.LOCATION = 2;
MsgFrom.NEAR_CHAT = 3;
MsgFrom.CHAT = 4;
MsgFrom.FIGHT_STARTED = 5;
MsgFrom.FIGHTER_SAID = 6;
MsgFrom.MOVE_AND_RESULTS = 7;
MsgFrom.MAKE_MOVE = 8;
MsgFrom.PEACE_PROPOSAL = 9;
MsgFrom.FIGHTER_END = 10;
MsgFrom.PEACE_ACCEPTANCE = 12;
MsgFrom.OTHER_HERO_INFO = 13;
MsgFrom.SERVER_MESSAGE = 14;
MsgFrom.TELEPORT = 15;
MsgFrom.SHOP_POSSESSIONS = 16;
MsgFrom.HERO_APPEARANCE = 17;
MsgFrom.PLAYER_INFO = 18;

MsgTo = {};
MsgTo.LOGIN_OR_REGISTER = 1;
MsgTo.LOCATION = 2;
MsgTo.NEAR_CHAT = 3;
MsgTo.CHAT = 4;
MsgTo.FIGHTER_SAID = 5;
MsgTo.MOVE = 6;
MsgTo.PEACE_ACCEPTANCE = 7;
MsgTo.OTHER_HERO_INFO = 8;
MsgTo.ATTACK = 9;
MsgTo.ENTERED_WORLD = 10;
MsgTo.READY_TO_ENTER = 11;
MsgTo.SHOP_POSSESSIONS = 12;
MsgTo.PEACE_PROPOSAL = 13;
MsgTo.TRAIN = 14;
MsgTo.MOVE_ITEM = 15;
MsgTo.BUY_ITEM = 16;
MsgTo.SELL_ITEM = 17;
MsgTo.MOVE_UNIT = 18;
MsgTo.SELL_UNIT = 19;

FightEndType = {};
FightEndType.WON = 1;
FightEndType.LOST = 2;
FightEndType.PEACE = 3;
FightEndType.KICKED = 4;