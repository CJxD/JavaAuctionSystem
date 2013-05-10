package com.cjwatts.auctionsystem.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cjwatts.auctionsystem.alert.Alerter;

public class FileHandlerTest {
	private FileHandler fh = new FileHandler();
	private String testObj = new String("Test");

	@BeforeClass
	public static void setUp() {
		Alerter.setHandler(new AuctionConsole());
		FileHandler.setDirectory("data/test");
	}
	
	@AfterClass
	public static void tearDown() {
		delete(new File("data/test"));
	}
	
	private static boolean delete(File file) {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			for (File f : children) {
				delete(f);
			}
		}
		return file.delete();
	}
	
	@Before
	public void selectDb() {
		try {
			fh.selectDb("users");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Test
	public void testWrite() throws IOException {
		fh.writeObject(testObj.hashCode(), testObj);
	}

	@Test
	public void testReadWrite() throws IOException {
		testWrite();
		String test = (String) fh.readObject(testObj.hashCode());
		assertEquals(test, testObj);
	}
	
	@Test
	public void testAutoIncrement() throws IOException {
		int current = fh.nextId();
		int given = fh.writeObject(testObj);
		assertEquals(current, given);
		assertEquals(current + 1, fh.nextId());
	}
}
