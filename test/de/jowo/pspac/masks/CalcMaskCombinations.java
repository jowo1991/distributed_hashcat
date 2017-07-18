package de.jowo.pspac.masks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CalcMaskCombinations {

	Path masksDir = Paths.get("G:\\Studium_Master\\Sem_2_SS17\\PS-PAC\\Project\\hashcat-3.5.0\\masks\\");

	static Map<String, Integer> maskToCombinations = new HashMap<>();

	@BeforeClass
	public static void setup() {
		maskToCombinations.put("?u", "ABCDEFGHIJKLMNOPQRSTUVWXYZ".length());
		maskToCombinations.put("?l", "abcdefghijklmnopqrstuvwxyz".length());
		maskToCombinations.put("?d", "0123456789".length());
		maskToCombinations.put("?s", "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~".length());
		maskToCombinations.put("?h", "0123456789abcdef".length());
		maskToCombinations.put("?H", "0123456789ABCDEF".length());
	}

	public void estimateFile(Path maskfile) {
		List<String> lines;
		try {
			lines = Files.readAllLines(maskfile);
		} catch (IOException e) {
			System.out.println("Failed to read: " + maskfile);
			return;
		}

		long sum = lines.stream().mapToLong(line -> {
			long num = calculateCombinations(line);
			// System.out.println(num + " - " + line);
			return num;
		}).sum();

		System.out.println(String.format("%,d \t\t %s", sum, maskfile.getFileName()));
	}

	public void estimateFileCumulative(Path maskfile) {
		List<String> lines;
		try {
			lines = Files.readAllLines(maskfile);
		} catch (IOException e) {
			System.out.println("Failed to read: " + maskfile);
			return;
		}

		List<Long> combinationsPerLine = lines.stream().map(line -> {
			long num = calculateCombinations(line);
			// System.out.println(num + " - " + line);
			return num;
		}).collect(Collectors.toList());

		long cumSum = 0;
		long combinations;
		for (int i = 0; i < combinationsPerLine.size(); i++) {
			combinations = combinationsPerLine.get(i);
			cumSum += combinations;

			System.out.println(String.format("[%d] %,d \t\t %s \t %,d", i, cumSum, lines.get(i), combinations));
		}
	}

	@Test
	public void estimateAll() throws IOException {
		Files.list(masksDir).forEach(this::estimateFile);
	}

	@Test
	public void estimateCumulative() throws IOException {
		estimateFileCumulative(masksDir.resolve("rockyou-1-60.hcmask"));
	}

	@Test
	public void test() {
		long num = calculateCombinations("?u?l?l?l?l?l?d?d");

		Assert.assertEquals(30891577600L, num);
	}

	public long calculateCombinations(String line) {
		long num = 1;
		for (int i = 0; i < line.length() - 1; i += 2) {
			String mask = new String(new char[] { line.charAt(i), line.charAt(i + 1) });
			num *= maskToCombinations.get(mask);
		}
		return num;
	}
}
