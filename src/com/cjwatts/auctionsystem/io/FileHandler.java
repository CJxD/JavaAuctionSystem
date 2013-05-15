package com.cjwatts.auctionsystem.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Set;

public class FileHandler implements PersistenceHandler {

	private static final String extension = "db";

	private static String directory = "data";
	
	private File file;
	private int autoincrement;
	private HashMap<Object, Object> cache;
	private Object cacheLock = new Object();

	@Override
	public void selectDb(String dbName) throws IOException {
		synchronized (cacheLock) {
			autoincrement = 0;
			cache = null;
			if (file == null) file = new File("");
			synchronized (file) {
				file = new File(directory + "/" + dbName + "." + extension);
			}
			readFile();
		}
	}

	@Override
	public int writeObject(Object value) throws IOException {
		int id = autoincrement;
		writeObject(id, value);
		return id;
	}
	
	@Override
	public void writeObject(Object key, Object value) throws IOException {
		synchronized (cacheLock) {
			// When the cache is invalidated, it is set to null
			if (cache == null && file.exists()) {
				// Try a read first
				readFile();
			} else if (cache == null) {
				// Otherwise, instantiate
				cache = new HashMap<Object, Object>();
			}
			autoincrement++;
			cache.put(key, value);
			writeFile();
		}
	}

	/**
	 * Write cache to disk
	 * 
	 * @throws IOException
	 */
	private void writeFile() throws IOException {
		// Make sure parent directory and file are ready
		synchronized (file) {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}

			// Hooray Java 7 try resource closing!
			try (FileOutputStream fos = new FileOutputStream(file);
					ObjectOutputStream oos = new ObjectOutputStream(fos)) {

				synchronized (cacheLock) {
					oos.writeInt(autoincrement);
					oos.writeObject(cache);
				}
			} catch (IOException ex) {
				throw ex;
			}
		}
	}

	@Override
	public Object readObject(Object key) throws IOException {
		synchronized (cacheLock) {
			// When the cache is invalidated, it is set to null
			if (cache == null)
				readFile();
			return cache.get(key);
		}
	}
	
	@Override
	public Set<Object> keySet() throws IOException {
		synchronized (cacheLock) {
			// When the cache is invalidated, it is set to null
			if (cache == null)
				readFile();
			return cache.keySet();
		}
	}

	/**
	 * Read cache from disk
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void readFile() throws IOException {
		synchronized (file) {
			if (!file.exists()) {
				autoincrement = 0;
				cache = new HashMap<Object, Object>();
				writeFile();
			} else {
				// Hooray Java 7 try resource closing!
				try (FileInputStream fis = new FileInputStream(file);
						ObjectInputStream ois = new ObjectInputStream(fis)) {

					int autoinc = ois.readInt();
					Object cacheObj = ois.readObject();
					synchronized (cacheLock) {
						autoincrement = autoinc;
						cache = (HashMap<Object, Object>) cacheObj;
					}
	
				} catch (IOException ex) {
					throw ex;
				} catch (ClassNotFoundException ex) {
					IOException iox = new IOException("Unable to load persistence");
					iox.initCause(ex);
					throw iox;
				}
			}
		}
	}

	@Override
	public int nextId() throws IOException {
		return autoincrement;
	}
	
	public static String getDirectory() {
		return directory;
	}

	public static void setDirectory(String directory) {
		FileHandler.directory = directory;
	}
}