package com.ibm.haploid.selfextractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class SelfExtractor {

	public static void main(String[] args) {
		try {
			new SelfExtractor(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private SelfExtractor(String[] args) throws Exception {
		extractJar();
		bootstrap(args);
	}

	private JarInputStream setJar() throws IOException {
		return new JarInputStream(new FileInputStream(getClass()
				.getProtectionDomain().getCodeSource().getLocation().getFile()));
	}

	private File setDirectory() throws IOException {
		File directory = Files.createTempDirectory(null).toFile();
		directory.deleteOnExit();
		return directory;
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

	private void bootstrap(String[] args) throws Exception {
		List<String> commands = new LinkedList<String>();
		commands.add("java");
		addOptions(commands);
		commands.add("-server");
		commands.add("-cp");
		commands.add(classpath.toString());
		commands.add(SelfExtractor.bootstrapperclass);
		ProcessBuilder processbuilder = new ProcessBuilder(commands);
		process.set(processbuilder.start());
		redirectOutput(process.get().getInputStream());
		redirectOutput(process.get().getErrorStream());
		redirectInput(process.get().getOutputStream());
		addShutdownHook();
		try {
			process.get().waitFor();
			process.set(null);
		} catch (InterruptedException e) {
			killChild();
		}
	}

	private void addOptions(List<String> commands) {
		Properties properties = System.getProperties();
		Iterator<Entry<Object, Object>> i = properties.entrySet().iterator();
		while (i.hasNext()) {
			Entry<Object, Object> entry = i.next();
			String k = entry.getKey().toString();
			String v = entry.getValue().toString();
			if (!k.startsWith("java") && !k.startsWith("sun")
					&& !k.startsWith("os") && !k.startsWith("user")
					&& !k.startsWith("file") && !k.startsWith("path")
					&& !k.startsWith("line") && !k.startsWith("awt")) {
				commands.add("-D" + k + "=" + v);
			}
		}
	}

	private void killChild() {
		if (null != process.get()) {
			PrintWriter processwriter = new PrintWriter(new OutputStreamWriter(
					process.get().getOutputStream()));
			processwriter.println("ctrl-c");
			processwriter.close();
			process.set(null);
			System.out.println("Killed bootstrapper process.");
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

	private void redirectInput(final OutputStream out) {
		Thread redirect = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					byte[] buffer = new byte[2048];
					int len = -1;
					while (-1 < (len = System.in.read(buffer))) {
						out.write(buffer, 0, len);
						out.flush();
					}
					out.flush();
				} catch (IOException e) {
				}
			}
		});
		redirect.setDaemon(true);
		redirect.start();
	}

	private void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				killChild();
			}
		}));
	}

	private final JarInputStream jar = setJar();
	private final File directory = setDirectory();
	private final StringBuilder classpath = new StringBuilder(1024);
	private final AtomicReference<Process> process = new AtomicReference<Process>();

	private static final String bootstrapperclass = "com.ibm.haploid.bootstrapper.BootStrapper";

}
