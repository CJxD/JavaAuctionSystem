package com.cjwatts.auctionsystem.mock;

import java.io.IOException;
import java.util.HashMap;

import com.cjwatts.auctionsystem.io.PersistenceHandler;

public class MockPersistenceHandler implements PersistenceHandler {

	public int autoincrement = 0;
	public HashMap<Object, Object> data = new HashMap<Object, Object>();

	@Override
	public void selectDb(String dbName) throws IOException {
	}

	@Override
	public int writeObject(Object value) throws IOException {
		int id = autoincrement;
		writeObject(id, value);
		return id;
	}
	
	@Override
	public void writeObject(Object key, Object value) throws IOException {
		data.put(key, value);
		autoincrement++;
	}

	@Override
	public Object readObject(Object key) throws IOException {
		return data.get(key);
	}

	@Override
	public int nextId() throws IOException {
		return autoincrement;
	}

}
