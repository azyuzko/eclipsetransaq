package ru.eclipsetrader.transaq.core.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Test {
	
	static class InnerTest implements Callable<String> {
		
		String uuid;
		
		public InnerTest() {
			uuid = UUID.randomUUID().toString();
		}

		@Override
		public String call() throws Exception {
			Thread.sleep(1000);
			System.out.println("call");
			return uuid;
		}
		
	}

	
	public static void main(String[] arg) throws InterruptedException, ExecutionException {
		ExecutorService es = Executors.newFixedThreadPool(2);
		
		List<Future<String>> lr = new ArrayList<Future<String>>();
		for (int i = 0; i < 10; i++) {
			Future<String> res = es.submit(new InnerTest());
			lr.add(res);
		}
		
		System.out.println("Complete schedule");
		for (Future<String> r : lr){
			System.out.println(r.get());
		}
		es.shutdown();
	}

}
