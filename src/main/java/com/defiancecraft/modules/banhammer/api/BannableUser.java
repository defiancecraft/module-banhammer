package com.defiancecraft.modules.banhammer.api;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.defiancecraft.core.api.User;
import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.database.collections.Users;
import com.defiancecraft.modules.banhammer.database.documents.DBBannableUser;
import com.defiancecraft.modules.banhammer.database.documents.DBBannableUser.DBBan;
import com.defiancecraft.modules.banhammer.database.documents.DBBannableUser.DBTempBan;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * An extension to the User API allowing for banning-type functionality
 * @author David
 *
 */
public class BannableUser {

	private User delegate;
	
	public BannableUser(User u) {
		this.delegate = u;
	}

	/**
	 * Bans the user, using the current time as the time of banning.
	 * 
	 * @param bannerUUID The player who banned them's UUID
	 * @param bannerName The player who banned them's name
	 * @param reason Why they were banned
	 * @return Whether the player was successfully banned.
	 * @throws MongoException If a database error occurred.
	 */
	public boolean ban(UUID bannerUUID, String bannerName, String reason) throws MongoException {
		return ban(new Date(), bannerUUID, bannerName, reason);
	}
	
	/**
	 * Bans the user
	 * 
	 * @param time The time that they were banned
	 * @param bannerUUID The player who banned them's UUID
	 * @param bannerName The player who banned them's name
	 * @param reason Why they were banned
	 * @return Whether the player was successfully banned.
	 * @throws MongoException If a database error occurred.
	 */
	public boolean ban(Date time, UUID bannerUUID, String bannerName, String reason) throws MongoException {
		
		DBBan ban = new DBBan(time, bannerUUID, bannerName, reason);
		
		DBObject query  = delegate.generateQuery();
		DBObject data = new BasicDBObject("$push", new BasicDBObject(DBBannableUser.FIELD_BAN_HISTORY, ban.getDBO())); 
		
		return Database.getCollection(Users.class).update(query, data).getN() > 0;
		
	}
	
	/**
	 * Unbans the user, using the current time as the
	 * time of appeal.
	 * 
	 * @return Whether they were unbanned successfully.
	 * @throws MongoException If there was a database error.
	 */
	public boolean unban() throws MongoException {
		return unban(new Date());
	}
	
	/**
	 * Unbans the user, using given time as the time
	 * of appeal.
	 * 
	 * @param time Time of unbanning
	 * @return Whether they were unbanned successfully.
	 * @throws MongoException If there was a database error.
	 */
	public boolean unban(Date time) throws MongoException {

		boolean tempbanUpdated = false;
		boolean bansUpdated = false;
		
		// Remove any effective tempbans
		if (getDBBU().isTempBanned()) {
			
			DBObject query = delegate.generateQuery();
			DBObject data  = new BasicDBObject("$unset", new BasicDBObject(DBBannableUser.FIELD_TEMPBAN, ""));
			
			tempbanUpdated = Database.getCollection(Users.class).update(query, data).getN() > 0;
			
		}
		
		// Appeal all permabans
		if (getDBBU().isBanned()) {
			
			List<DBBan> newBanHistory = getDBBU().getBanHistory();
			for (DBBan ban : newBanHistory) {
				if (!DBBan.isAppealed(ban))
					ban.setAppealed(time);
			}
			
			DBObject query = delegate.generateQuery();
			DBObject data  = new BasicDBObject("$set",
					new BasicDBObject(DBBannableUser.FIELD_BAN_HISTORY,
							newBanHistory
								.stream()
								.map((ban) -> ban.getDBO())
								.collect(Collectors.toList())));
			
			bansUpdated = Database.getCollection(Users.class).update(query, data).getN() > 0;
			
		}
		
		return bansUpdated || tempbanUpdated;
		
	}
	
	/**
	 * Temporarily bans a user for `duration` seconds.
	 * 
	 * @param duration Duration to ban the user for
	 * @return Whether they were successfully tempbanned.
	 */
	public boolean tempBan(int duration) {
		
		DBObject query = delegate.generateQuery();
		DBObject data  = new BasicDBObject("$set", new BasicDBObject(DBBannableUser.FIELD_TEMPBAN, new DBTempBan(new Date(), duration).getDBO()));
		
		return Database.getCollection(Users.class).update(query, data).getN() > 0;
		
	}
	
	/**
	 * Checks whether a user is permanently banned.
	 * @return Whether the user is permanently banned.
	 */
	public boolean isBanned() {
		
		return new DBBannableUser(delegate.getDBU()).isBanned();
		
	}
	
	/**
	 * Checks whether a user has an active temp-ban.
	 * @return Whether a user is temp-banned.
	 */
	public boolean isTempBanned() {
		
		return new DBBannableUser(delegate.getDBU()).isTempBanned();
		
	}
	
	/**
	 * Checks whether the user is allowed to play on the server.
	 * @return Whether the user may play on the server.
	 */
	public boolean isAllowedOnServer() {
		return !isBanned() && !isTempBanned();
	}
	
	/**
	 * Gets the User this BannableUser represents.
	 * @return User
	 */
	public User getUser() {
		return this.delegate;
	}
	
	/**
	 * Gets a DBBannableUser using User#getDBU()
	 * @return DBBannableUser
	 */
	public DBBannableUser getDBBU() {
		return new DBBannableUser(this.delegate.getDBU());
	}
	
	public String getRemainingTempbanDuration() {

		Date start = new Date();
		Date end = this.getDBBU().getTempBan().getEndDate();
		
		Duration dur = Duration.between(start.toInstant(), end.toInstant());
		long hours = dur.toHours();
		long mins  = dur.toMinutes() % 60;
		long secs  = dur.getSeconds() % 60;
		
		return String.format("%02d:%02d:%02d", hours, mins, secs);
		
	}

}
