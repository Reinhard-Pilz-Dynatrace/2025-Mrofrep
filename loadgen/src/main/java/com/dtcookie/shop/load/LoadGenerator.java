package com.dtcookie.shop.load;

import java.io.InputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dtcookie.shop.Ports;
import com.dtcookie.util.Streams;

public class LoadGenerator extends TimerTask {

	private static final Timer timer = new Timer(false);
	private static final ExecutorService executor = Executors.newCachedThreadPool();

	public static void main(String[] args) throws Exception {		
		timer.schedule(new LoadGenerator(), 0, 1000);
	}

	@Override
	public void run() {
		executor.submit(LoadGenerator::sendRequest);
	}

	public static Object sendRequest() throws Exception {
		URL url = new URL("http://order-api-" + System.getenv("GITHUB_USER") + ":" + Ports.FRONTEND_LISTEN_PORT + "/place-order");
		try (InputStream in = url.openStream()) {
			Streams.drain(in);
		}
		return null;
	}
}
