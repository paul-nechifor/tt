/**
 * Class Clan
 */
/**
 * Constructor for Clan
 */
function Clan() {
	this._name = null;
	this._players = new Array();
	this._announcements = new Array();
}
/**
 * Setter for attribute name
 * 
 * @param name
 */
Clan.prototype.setName = function(name) {
	this._name = name;
};
/**
 * Add a player into players list
 * 
 * @param player
 */
Clan.prototype.addPlayer = function(player) {
	// this._players.add(player);
};
/**
 * Remove a player from players list
 * 
 * @param player
 */
Clan.prototype.remPlayer = function(player) {
	// this._players.rem(player);
};
/**
 * Add an announcement into announcements list
 * 
 * @param announce
 */
Clan.prototype.addAnnouncement = function(announce) {
	// this._announcements.add(announce);
};
