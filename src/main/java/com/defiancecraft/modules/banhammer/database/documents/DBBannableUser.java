package com.defiancecraft.modules.banhammer.database.documents;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.defiancecraft.core.database.documents.DBUser;
import com.defiancecraft.core.database.documents.Document;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * This is an extension of the DBUser class to implement fields
 * such as 'banned' using the decorator pattern.
 */
public class DBBannableUser extends DBUser {

	public static final String FIELD_BAN_HISTORY = "ban_history";
	public static final String FIELD_TEMPBAN = "tempban";
	
	private DBUser delegate;
	
	public DBBannableUser(DBUser u) {
		super(u.getDBO());
		this.delegate = u;
	}
	
	public DBUser getDBUser() {
		return this.delegate;
	}
	
	public List<DBBan> getBanHistory() {
		return getDBObjectList(FIELD_BAN_HISTORY, new ArrayList<DBObject>())
				.stream()
				.map((b) -> new DBBan(b))
				.collect(Collectors.toList());
	}
	
	public DBTempBan getTempBan() {
		return new DBTempBan(getDBObject(FIELD_TEMPBAN, new BasicDBObject()));
	}
	
	public boolean isBanned() {
		
		// Check's that field is present, and at least one
		// ban isn't appealed.
		return getDBO().containsField(FIELD_BAN_HISTORY)
				&& !getBanHistory()
					.stream()
					.allMatch(DBBan::isAppealed);
		
	}
	
	/**
	 * Checks whether a user is still temp-banned using the
	 * current date.
	 * 
	 * @return Whether the user is temp-banned.
	 */
	public boolean isTempBanned() {
		
		return getDBO().containsField(FIELD_TEMPBAN)
				&& getTempBan().getEndDate().after(new Date());
		
	}
	
	/**
	 * A Document representing a ban in a user's ban
	 * history. The time, banner, reason, and whether
	 * it was appealed (they were unbanned) is recorded.
	 */
	public static class DBBan extends Document {

		public static final String FIELD_TIME = "time";
		public static final String FIELD_BANNERNAME = "banner_name";
		public static final String FIELD_BANNERUUID = "banner_uuid";
		public static final String FIELD_REASON = "reason";
		public static final String FIELD_APPEALED = "appealed";
		
		public DBBan(DBObject obj) {
			super(obj);
		}
		
		/**
		 * Constructs a new DBBan
		 * 
		 * @param time The time of the ban
		 * @param bannerUUID The UUID of the person who banned them (optional)
		 * @param bannerName The name of the person who banned them
		 * @param reason The reason given for the ban
		 */
		public DBBan(Date time, UUID bannerUUID, String bannerName, String reason) {
			super(new BasicDBObject());
			getDBO().put(FIELD_TIME, time);
			getDBO().put(FIELD_BANNERNAME, bannerName);
			getDBO().put(FIELD_REASON, reason);
			
			// Can be null if console
			if (bannerUUID != null)
				getDBO().put(FIELD_BANNERUUID, bannerUUID.toString());
		}
		
		public UUID getBannerUUID() {
			return getDBO().containsField(FIELD_BANNERUUID) ? UUID.fromString(getString(FIELD_BANNERUUID)) : null;
		}
		
		public String getBannerName() {
			return getString(FIELD_BANNERNAME);
		}
		
		public String getReason() {
			return getString(FIELD_REASON);
		}
		
		public void setAppealed(Date appealTime) {
			getDBO().put(FIELD_APPEALED, appealTime);
		}
		
		public static boolean isAppealed(DBBan obj) {
			return obj.getDBO().containsField(FIELD_APPEALED);
		}
		
	}
	
	/**
	 * A Document representing a temporary ban.
	 */
	public static class DBTempBan extends Document {
		
		public static final String FIELD_DATE = "date";
		public static final String FIELD_DURATION = "duration"; // Duration in seconds.

		public DBTempBan(DBObject obj) {
			super(obj);
		}
		
		/**
		 * Constructs a new DBTempBan
		 * 
		 * @param date Date the tempban was created
		 * @param duration The duration, <b>in seconds</b> of the tempban.
		 */
		public DBTempBan(Date date, int duration) {
			super(new BasicDBObject());
			getDBO().put(FIELD_DATE, date);
			getDBO().put(FIELD_DURATION, duration);
		}
		
		public Date getDate() {
			return getDate(FIELD_DATE);
		}
		
		public int getDuration() {
			return getInt(FIELD_DURATION);
		}
		
		public Date getEndDate() {
			return Date.from(getDate().toInstant().plusSeconds(getDuration()));
		}
		
	}
	
}
