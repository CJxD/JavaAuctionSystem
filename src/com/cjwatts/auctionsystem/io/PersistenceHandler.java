package com.cjwatts.auctionsystem.io;

import java.io.IOException;
import java.util.Set;

public interface PersistenceHandler {

	/**
	 * Select a database by name
	 * 
	 * @param dbName
	 *            Name of database
	 * @throws IOException If database could not be instantiated
	 */
	public void selectDb(String dbName) throws IOException;

	/**
	 * Write object to persistence, using the autoincrementer as
	 * the key value.
	 * 
	 * @param value
	 *            Object to write
	 * @return The key value given
	 * @throws IOException If database could not be correctly written to
	 */
	public int writeObject(Object value) throws IOException;
	
	/**
	 * Write object to persistence
	 * 
	 * @param key
	 *            Identifying key (use o.hashCode() if in doubt)
	 * @param value
	 *            Object to write
	 * @throws IOException If database could not be correctly written to
	 */
	public void writeObject(Object key, Object value) throws IOException;

	/**
	 * Read object from persistence
	 * 
	 * @param key
	 *            Identifying key
	 * @throws IOException If database could not be correctly read
	 */
	public Object readObject(Object key) throws IOException;
	
	/**
	 * @return List of all keys in the database
	 * @throws IOException If database could not be correctly read
	 */
	public Set<Object> keySet() throws IOException;

	/**
	 * @return Next available ID number for insertion
	 * @throws IOException If database could not be correctly read
	 */
	public int nextId() throws IOException;
}
