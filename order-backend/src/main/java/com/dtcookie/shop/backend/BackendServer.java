package com.dtcookie.shop.backend;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dtcookie.database.Database;
import com.dtcookie.shop.Ports;
import com.dtcookie.shop.Product;
import com.dtcookie.util.GETRequest;
import com.dtcookie.util.Http;
import com.dtcookie.util.Otel;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.semconv.SemanticAttributes;

public class BackendServer {

	private static final Logger log = LogManager.getLogger(BackendServer.class);

	private static OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
	private static final Tracer tracer = openTelemetry.getTracer("manual-instrumentation");
	private static final TextMapGetter<HttpServletRequest> getter = Otel.newRequestHeaderGetter();

	private static final ExecutorService executor = Executors.newCachedThreadPool();
	private static final Timer creditCardFullScanTimer = new Timer(true);

	public static void main(String[] args) throws Exception {
		creditCardFullScanTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("PERFORMING FULL CREDIT CARD SCAN");
				for (int i = 0; i < 35; i++) {
					executor.submit(BackendServer::performFullCreditCardScan);
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}, 1000 * 60 * 2, 1000 * 60 * 15);

		Database.Debug.set(true);
		log.info("Launching Backend Server");
		openTelemetry = Otel.init();
		Http.serve("order-backend-credit-cards" + System.getenv("GITHUB_USER"), Ports.CREDIT_CARD_LISTEN_PORT, "/validate-credit-card/*", BackendServer::handleCreditcards);
		Http.serve("order-backend-inventory" + System.getenv("GITHUB_USER"), Ports.INVENTORY_LISTEN_PORT, "/check-inventory/*", BackendServer::handleInventory);
	}

	public static UUID process(Product product) throws Exception {
		Span span = tracer.spanBuilder("process").setSpanKind(SpanKind.INTERNAL).startSpan();
		try (Scope scope = span.makeCurrent()) {
			span.setAttribute("product.name", product.getName());
			Database.execute("SELECT pattern FROM credit_card_patterns WHERE vendor = '" + product.getID() + "'");
			for (int i = 0; i < 1 + ThreadLocalRandom.current().nextLong(1); i++) {
				executor.submit(BackendServer::postProcess);
			}
			notifyProcessingBackend(product);
			return UUID.randomUUID();
		} catch (Exception e) {
			span.recordException(e);
			span.setStatus(StatusCode.ERROR);
			throw e;
		} finally {
			span.end();
		}
	}

	public static void notifyProcessingBackend(Product product) throws Exception {
		
		GETRequest request = new GETRequest("http://order-quotes-" + System.getenv("GITHUB_USER") + ":" + "8090/quote");
		// GETRequest request = new GETRequest("http://<replace with remote IP address>/app");
		request.send();
	}

	public static String handleCreditcards(HttpServletRequest request) throws Exception {
		String requestURI = request.getRequestURI();
		String productID = requestURI.substring(requestURI.lastIndexOf("/") + 1);

		Context ctx = openTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), request, getter);

		try (Scope ctScope = ctx.makeCurrent()) {
			Span serverSpan = tracer.spanBuilder(request.getRequestURI()).setSpanKind(SpanKind.SERVER)
					.startSpan();
			try (Scope scope = serverSpan.makeCurrent()) {
				serverSpan.setAttribute(SemanticAttributes.HTTP_REQUEST_METHOD,	request.getMethod().toUpperCase());
				serverSpan.setAttribute(SemanticAttributes.URL_SCHEME, "http");				
				serverSpan.setAttribute(SemanticAttributes.SERVER_ADDRESS, "order-backend-" + System.getenv("GITHUB_USER") + ":" + Ports.CREDIT_CARD_LISTEN_PORT);
				serverSpan.setAttribute(SemanticAttributes.URL_PATH, request.getRequestURI());

				UUID result = process(Product.random());
				serverSpan.setAttribute(SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, 200);
				executor.submit(Purchase.confirm(productID));
				return result.toString();
			} catch (Exception e) {
				serverSpan.setAttribute(SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, 500);
				serverSpan.recordException(e);
				serverSpan.setStatus(StatusCode.ERROR);
				log.warn("credit card validation failed", e);
				throw e;
			} finally {
				serverSpan.end();
			}
		}
	}

	public static String handleInventory(HttpServletRequest request) throws Exception {
		String url = request.getRequestURI();
		String productName = url.substring(url.lastIndexOf("/"));
		int quantity = 1;				

		Context ctx = openTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), request, getter);

		try (Scope ctScope = ctx.makeCurrent()) {
			Span serverSpan = tracer.spanBuilder(request.getRequestURI()).setSpanKind(SpanKind.SERVER)
					.startSpan();
			try (Scope scope = serverSpan.makeCurrent()) {
				serverSpan.setAttribute(SemanticAttributes.HTTP_REQUEST_METHOD,	request.getMethod().toUpperCase());
				serverSpan.setAttribute(SemanticAttributes.URL_SCHEME, "http");				
				serverSpan.setAttribute(SemanticAttributes.SERVER_ADDRESS, "order-backend-" + System.getenv("GITHUB_USER") + ":" + Ports.INVENTORY_LISTEN_PORT);
				serverSpan.setAttribute(SemanticAttributes.URL_PATH, request.getRequestURI());

				Database.execute("SELECT * FROM products WHERE name = '" + productName + "'");

				checkStorageLocations(productName, quantity);

				serverSpan.setAttribute(SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, 200);
				return "done";
			} catch (Exception e) {
				serverSpan.setAttribute(SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, 500);
				serverSpan.recordException(e);
				serverSpan.setStatus(StatusCode.ERROR);
				log.warn("checking inventory failed", e);
				throw e;
			} finally {
				serverSpan.end();
			}
		}
	}

	public static void checkStorageLocations(String productName, int quantity) {
		Span span = tracer.spanBuilder("check-storage-locations").setSpanKind(SpanKind.INTERNAL).startSpan();
		try (Scope scope = span.makeCurrent()) {
			boolean deducted = false;
			for (StorageLocation location : StorageLocation.getAll()) {
				if (location.available(productName, quantity)) {
					deductFromLocation(location, productName, quantity);
					deducted = true;
					break;
				}
			}
			if (!deducted) {
				span.addEvent("nothing deducted", io.opentelemetry.api.common.Attributes.builder().put("product.name", productName).build());
				span.setStatus(StatusCode.ERROR);
			}
		} finally {
			span.end();
		}
	}
	
	public static void deductFromLocation(StorageLocation location, String productName, int quantity) {
		Span span = tracer.spanBuilder("deduct").setSpanKind(SpanKind.INTERNAL).startSpan();
		try (Scope scope = span.makeCurrent()) {
			span.setAttribute("product.name", productName);
			span.setAttribute("location.name", location.getName());
			span.setAttribute("quantity", quantity);
			location.deduct(productName, quantity);
		} finally {
		  span.end();
		}
	}

	public static Object postProcess() throws Exception {
		Span span = tracer.spanBuilder("post-process").setSpanKind(SpanKind.INTERNAL).startSpan();
		try (Scope scope = span.makeCurrent()) {
			Database.execute("SELECT * FROM inventory WHERE product = '" + UUID.randomUUID().toString() + "'");
		} catch (Exception e) {
			span.setStatus(StatusCode.ERROR);
			span.recordException(e);
			throw e;
		} finally {
			span.end();
		}
		return null;
	}

	public static Object performFullCreditCardScan() throws Exception {
		long start = System.currentTimeMillis();
		try (Connection con = Database.getConnection(60, TimeUnit.SECONDS)) {
			Thread.sleep(50 * (System.currentTimeMillis() - start));
			if (con != null) {
				try (Statement stmt = con.createStatement()) {
					stmt.executeUpdate(
							"SELECT * FROM credit_card WHERE number = '" + UUID.randomUUID().toString() + "'");
				}
			} else {
				executor.submit(BackendServer::performFullCreditCardScan);
			}
		}
		return null;
	}

}