package com.ibm.haploid.selfextractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import sun.misc.Signal;
import sun.misc.SignalHandler;

@SuppressWarnings("restriction")
public class SelfExtractor {

	public static void main(String[] args) {
		try {
			final SelfExtractor selfextractor = new SelfExtractor();
			selfextractor.extractJar();
			selfextractor.bootstrap(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(childexitcode);
	}

	private JarInputStream setJar() {
		try {
			return new JarInputStream(new FileInputStream(getClass()
					.getProtectionDomain().getCodeSource().getLocation()
					.getFile()));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private File setDirectory() {
		try {
			File directory;
			directory = Files.createTempDirectory(null).toFile();
			directory.deleteOnExit();
			return directory;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void extractJar() throws Exception {
		JarEntry entry;
		while (null != (entry = jar.getNextJarEntry())) {
			if (!entry.isDirectory() && entry.getName().endsWith(".jar")) {
				File file = new File(directory, entry.getName());
				file.deleteOnExit();
				Files.copy(jar, file.toPath());
				if (0 < classpath.length())
					classpath.append(File.pathSeparator);
				classpath.append(file.getAbsolutePath());
			}
		}
		jar.close();
	}

	private void bootstrap(String[] args) {
		try {
			List<String> commands = new LinkedList<String>();
			commands.add("java");
			addOptions(commands);
			commands.add("-Dhaploid.bootstrapping=on");
			commands.add("-server");
			commands.add("-cp");
			commands.add(classpath.toString());
			commands.add(SelfExtractor.bootstrapperclass);
			ProcessBuilder processbuilder = new ProcessBuilder(commands);
			processbuilder.redirectInput(Redirect.PIPE);
			processbuilder.redirectOutput(Redirect.PIPE);
			process.set(processbuilder.start());
			processwriter = new PrintWriter(new OutputStreamWriter(process
					.get().getOutputStream()));
			redirectOutput(process.get().getInputStream());
			redirectOutput(process.get().getErrorStream());
			addShutdownHook();
			childexitcode = process.get().waitFor();
			process.set(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addOptions(List<String> commands) {
		Properties properties = System.getProperties();
		Iterator<Entry<Object, Object>> i = properties.entrySet().iterator();
		while (i.hasNext()) {
			Entry<Object, Object> entry = i.next();
			String k = entry.getKey().toString();
			String v = entry.getValue().toString();
			if (k.equals("java.io.tmpdir")
					|| (!k.startsWith("java") && !k.startsWith("sun")
							&& !k.startsWith("os") && !k.startsWith("user")
							&& !k.startsWith("file") && !k.startsWith("path")
							&& !k.startsWith("line") && !k.startsWith("awt"))) {
				commands.add("-D" + k + "=" + v);
			}
		}
	}

	private void redirectOutput(final InputStream in) {
		Thread redirect = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					byte[] buffer = new byte[2048];
					int len = -1;
					while (-1 < (len = in.read(buffer))) {
						System.out.write(buffer, 0, len);
						System.out.flush();
					}
				} catch (IOException e) {
				}
				System.out.flush();
			}
		});
		redirect.setDaemon(true);
		redirect.start();
	}

	private class KillSignalHandler implements SignalHandler {

		@Override
		public void handle(Signal signal) {
			processwriter.println("ctrl-c");
			processwriter.flush();
			System.out.println("ctrl-c");
		}

	}

	private void addShutdownHook() {
		Signal.handle(new Signal("INT"), new KillSignalHandler());
		Signal.handle(new Signal("TERM"), new KillSignalHandler());
		Signal.handle(new Signal("ABRT"), new KillSignalHandler());
	}

	private final JarInputStream jar = setJar();
	private final File directory = setDirectory();
	private final StringBuilder classpath = new StringBuilder(1024);
	private final AtomicReference<Process> process = new AtomicReference<Process>();
	private PrintWriter processwriter = null;

	private static int childexitcode = 0;

	private static final String bootstrapperclass = "com.ibm.haploid.bootstrapper.BootStrapper";

}
