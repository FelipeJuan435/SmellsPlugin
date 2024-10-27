package com.github.FelipeJuan435;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

@Mojo(name = "detect-smells", defaultPhase = LifecyclePhase.TEST)
public class MyMojo extends AbstractMojo {

	@Parameter(property = "testSmellDetector.jarPath", required = true)
	private String jarPath;

	@Parameter(property = "testSmellDetector.inputCsvPath", required = true)
	private String inputCsvPath;

	public void execute() throws MojoExecutionException {
		// Validar que los archivos existen
		File jarFile = new File(jarPath);
		File inputCsvFile = new File(inputCsvPath);

		if (!jarFile.exists()) {
			throw new MojoExecutionException("El archivo TestSmellDetector.jar no se encuentra en la ruta especificada: " + jarPath);
		}
		if (!inputCsvFile.exists()) {
			throw new MojoExecutionException("El archivo CSV de entrada no se encuentra en la ruta especificada: " + inputCsvPath);
		}

		// Ejecutar TestSmellDetector.jar
		try {
			ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", jarFile.getAbsolutePath(), inputCsvFile.getAbsolutePath());
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();

			// Leer y mostrar la salida del comando
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				getLog().info(line);
			}

			int exitCode = process.waitFor();
			if (exitCode != 0) {
				throw new MojoExecutionException("TestSmellDetector.jar terminó con un código de error: " + exitCode);
			}

			// Leer el archivo de salida
			File dir = new File(".");
			// Listar archivos en el directorio
			File[] files = dir.listFiles();

			if (files != null) {
				for (File file : files) {
					// Verificar si el archivo comienza con "Output_TestSmellDetection_"
					if (file.isFile() && file.getName().startsWith("Output_TestSmellDetection_")) {
						try (BufferedReader br = new BufferedReader(new FileReader(file))) {
							// Leer el contenido del archivo línea por línea
							while ((line = br.readLine()) != null) {
								System.out.println(line); // Imprimir cada línea
							}
						} catch (IOException e) {
							System.err.println("Error al leer el archivo: " + e.getMessage());
						}
					}
				}
			} else {
				System.out.println("No se encontraron archivos en el directorio.");
			}


		} catch (Exception e) {
			throw new MojoExecutionException("Error ejecutando TestSmellDetector.jar", e);
		}
	}
}
