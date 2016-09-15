package com.khorium.publicfarm.database;

import java.sql.Connection;

public interface DatabaseCore {
	public Connection getConnection();

	public void queue(BufferStatement bs);

	public void flush();

	public void close();

	Connection getConnectionTest();
}