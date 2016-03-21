/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.reactive;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.reactivestreams.Subscriber;
import org.reactivestreams.example.unicast.SyncSubscriber;
import org.reactivestreams.tck.SubscriberBlackboxVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class SyncSubscriberTest extends SubscriberBlackboxVerification<Integer> {

	private ExecutorService e;
	@BeforeClass void before() { e = Executors.newFixedThreadPool(4); }
	@AfterClass void after() { if (e != null) e.shutdown(); }

	public SyncSubscriberTest() {
		super(new TestEnvironment());
	}

	@Override public Subscriber<Integer> createSubscriber() {
		return new SyncSubscriber<Integer>() {
			private long acc;
			@Override protected boolean foreach(final Integer element) {
				acc += element;
				return true;
			}

			@Override public void onComplete() {
				System.out.println("Accumulated: " + acc);
			}
		};
	}

	@Override public Integer createElement(int element) {
		return element;
	}
}
