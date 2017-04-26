package com.twinsoft.convertigo.eclipse.swt;

import java.awt.Frame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Composite;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserContext;
import com.teamdev.jxbrowser.chromium.BrowserPreferences;
import com.teamdev.jxbrowser.chromium.events.FailLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.FrameLoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadListener;
import com.teamdev.jxbrowser.chromium.events.ProvisionalLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.StartLoadingEvent;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import com.twinsoft.convertigo.engine.Engine;

public class C8oBrowser extends Composite {
	
	static {
		int port = 18082;
		BrowserPreferences.setChromiumSwitches("--remote-debugging-port=" + port);
	}
	
	private static Thread threadSwt = null;

	private BrowserView browserView;

	public C8oBrowser(Composite parent, int style) {
		this(parent, style, BrowserContext.defaultContext());
	}

	public C8oBrowser(Composite parent, int style, BrowserContext browserContext) {
		super(parent, style | SWT.EMBEDDED | SWT.NO_BACKGROUND);
	    Frame frame = SWT_AWT.new_Frame(this);
	    browserView = new BrowserView(new Browser(browserContext));
		frame.add(browserView);
		threadSwt = parent.getDisplay().getThread();
	}
	
	@Override
	public void dispose() {
		run(() -> {
			getBrowser().dispose();			
		});
		super.dispose();
	}

	public BrowserView getBrowserView() {
		return browserView;
	}

	public Browser getBrowser() {
		return browserView.getBrowser();
	}
	
	public void setText(String html) {
		getBrowser().loadHTML(html);
	}

	public void setUrl(String url) {
		getBrowser().loadURL(url);
	}
		
	@Override
	public boolean setFocus() {
		C8oBrowser.run(() -> browserView.requestFocus());
		return super.setFocus();
	}

	public void addProgressListener(ProgressListener progressListener) {
		getBrowser().addLoadListener(new LoadListener() {
			
			@Override
			public void onStartLoadingFrame(StartLoadingEvent event) {
			}
			
			@Override
			public void onProvisionalLoadingFrame(ProvisionalLoadingEvent event) {
			}
			
			@Override
			public void onFinishLoadingFrame(FinishLoadingEvent event) {
				progressListener.completed(null);
			}
			
			@Override
			public void onFailLoadingFrame(FailLoadingEvent event) {
				
			}
			
			@Override
			public void onDocumentLoadedInMainFrame(LoadEvent event) {
			}
			
			@Override
			public void onDocumentLoadedInFrame(FrameLoadEvent event) {
				
			}
		});
	}
	
	public static void run(Runnable runnable) {
		if (threadSwt != null && threadSwt.equals(Thread.currentThread())) {
			Engine.execute(runnable);
		} else {
			runnable.run();
		}
	}
}
