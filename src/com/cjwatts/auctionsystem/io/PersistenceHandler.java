package com.cjwatts.auctionsystem.io;

import java.io.IOException;

public interface PersistenceHandler {

	/**
	 * Select a database by name
	 * 
	 * @param dbName
	 *            Name of database
	 */
	public void selectDb(String dbName) throws IOException;

	/**
	 * Write object to persistence, using the autoincrementer as
	 * the key value.
	 * 
	 * @param value
	 *            Object to write
	 * @return The key value given
	 */
	public int writeObject(Object value) throws IOException;
	
	/**
	 * Write object to persistence
	 * 
	 * @param key
	 *            Identifying key (use o.hashCode() if in doubt)
	 * @param value
	 *            Object to write
	 */
	public void writeObject(Object key, Object value) throws IOException;

	/**
	 * Read object from persistence
	 * 
	 * @param key
	 *            Identifying key
	 */
	public Object readObject(Object key) throws IOException;

	/**
	 * @return Next available ID number for insertion
	 */
	public int nextId() throws IOException;
}
